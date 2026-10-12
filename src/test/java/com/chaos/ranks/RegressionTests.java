package com.chaos.ranks;

import com.chaos.ranks.rank.*;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.badge.BadgeMeta;
import com.chaos.ranks.client.MenuGeometry;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import java.nio.file.*;
import java.util.*;

/** Executable regressions; a nonzero exit fails Gradle check/build. */
public final class RegressionTests {
 private static int assertions;
 private static final Gson GSON = new Gson();
 private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
 private static void check(boolean condition, String message) {
  assertions++; if (!condition) throw new AssertionError(message);
 }
 private interface Action { void run() throws Exception; }
 private static void fails(Action action, String message) throws Exception {
  boolean failed = false;
  try { action.run(); } catch (Exception expected) { failed = true; }
  check(failed, message);
 }
 private static Path temp() throws Exception { return Files.createTempDirectory(Path.of("."), "rank-test-"); }
 private static RankDef rank(String id) { RankDef d = new RankDef(); d.id = id; d.label = id.toUpperCase(Locale.ROOT); d.icon = "star"; return d; }
 private static RankPersistence.Snapshot snapshot(Path dir) throws Exception { return RankPersistence.decode(Files.readString(dir.resolve("store.json"))); }
 public static void main(String[] args) throws Exception {
  Path world = temp(); RankStore.load(world);
  check(RankStore.ranks().size() == 2, "fresh defaults");
  check(snapshot(world).seeded(), "seed flag committed with data");
  RankDef vip = rank("vip"); RankStore.put(vip); RankStore.assign(PLAYER, "vip");
  RankDef owner = RankStore.get("vip"); owner.id = "owner"; owner.label = "OWNER";
  RankStore.put("vip", owner);
  check(snapshot(world).players().get(PLAYER).equals("owner"), "rename persists assignment together");
  RankStore.load(world); check(RankStore.get("vip") == null && RankStore.get("owner") != null, "rename survives reload");
  long before = RankStore.revision(); RankStore.assignAll(List.of(PLAYER, UUID.randomUUID(), UUID.randomUUID()), "owner");
  check(RankStore.revision() == before + 1, "bulk assignment is one transaction");
  fails(() -> RankStore.requireRevision(before), "stale editor rejected");
  RankStore.requireRevision(RankStore.revision());
  RankDef external = RankStore.get("owner"); external.label = "MUTATED";
  check(RankStore.get("owner").label.equals("OWNER"), "get returns a copy");
  RankStore.ranks().iterator().next().label = "MUTATED";
  check(!RankStore.ranks().iterator().next().label.equals("MUTATED"), "list returns copies");
  owner = RankStore.get("owner"); owner.defaultRank = true; owner.opRank = true; RankStore.put("owner", owner);
  check(RankStore.ranks().stream().filter(d -> d.defaultRank).count() == 1, "single default rank");
  check(RankStore.ranks().stream().filter(d -> d.opRank).count() == 1, "single OP rank");
  RankStore.delete("owner"); check(snapshot(world).players().isEmpty(), "delete cleans assignments");
  RankStore.load(world); check(RankStore.ranks().stream().noneMatch(d -> d.defaultRank || d.opRank), "deliberately deleted fallback is not reseeded");

  // A failed write must leave both the committed file and the in-memory state unchanged.
  Path failure = temp(); RankStore.load(failure);
  byte[] committed = Files.readAllBytes(failure.resolve("store.json")); long revision = RankStore.revision();
  Files.move(failure.resolve("store.json.bak"), failure.resolve("held-backup.json"));
  Files.createDirectory(failure.resolve("store.json.bak"));
  fails(() -> RankStore.put(rank("failure")), "write failure is surfaced");
  check(RankStore.get("failure") == null && RankStore.revision() == revision, "write failure rolls back memory");
  check(Arrays.equals(committed, Files.readAllBytes(failure.resolve("store.json"))), "write failure preserves snapshot");

  for (String bad : List.of("{", "null", "{\"vip\":null}", "{\"vip\":{\"id\":\"other\",\"label\":\"VIP\"}}", "{\"bad-id\":{\"id\":\"bad-id\",\"label\":\"VIP\"}}", "{\"vip\":{\"id\":\"vip\",\"label\":\" \"}}")) {
   Path damaged = temp(); Files.writeString(damaged.resolve("ranks.json"), bad);
   fails(() -> RankStore.load(damaged), "reject damaged legacy rank data");
   check(Files.readString(damaged.resolve("ranks.json")).equals(bad), "preserve damaged source");
   check(!Files.exists(damaged.resolve("store.json")), "do not migrate damaged source");
   fails(() -> RankStore.put(rank("new")), "damaged data disables writes");
  }
  Path legacy = temp(); RankDef manual = rank("vip");
  Files.writeString(legacy.resolve("ranks.json"), GSON.toJson(Map.of("vip", manual)));
  Files.writeString(legacy.resolve("players.json"), GSON.toJson(Map.of(PLAYER, "vip")));
  Files.writeString(legacy.resolve("state.json"), "{\"seeded\":true}");
  byte[] original = Files.readAllBytes(legacy.resolve("ranks.json"));
  RankStore.load(legacy); check(snapshot(legacy).players().get(PLAYER).equals("vip"), "legacy assignment migrated");
  check(Arrays.equals(original, Files.readAllBytes(legacy.resolve("ranks.json"))), "legacy files retained");
  RankStore.put(rank("second"));
  Files.writeString(legacy.resolve("store.json"), "{broken");
  RankStore.load(legacy); check(RankStore.get("vip") != null, "recover valid backup");
  try (var files = Files.list(legacy)) { check(files.anyMatch(p -> p.getFileName().toString().startsWith("store.corrupt-")), "archive corrupt primary"); }
  RankStore.put(rank("recovered")); check(snapshot(legacy).ranks().containsKey("recovered"), "save recovered state");
  Files.writeString(legacy.resolve("store.json"), "{"); Files.writeString(legacy.resolve("store.json.bak"), "{");
  fails(() -> RankStore.load(legacy), "both damaged snapshots rejected");
  check(!RankStore.writable(), "both damaged snapshots disable writes");
  Path missing = temp(); RankStore.load(missing); Files.delete(missing.resolve("store.json"));
  RankStore.load(missing); check(RankStore.ranks().size() == 2, "missing primary recovers backup");
  RankStore.put(rank("next")); String valid = Files.readString(missing.resolve("store.json"));
  Files.writeString(missing.resolve("store.json"), valid.replace("\"schema\": 1", "\"schema\": 2"));
  fails(() -> RankStore.load(missing), "future schema refuses rollback");

  Path duplicate = temp(); RankDef a = rank("a"), b = rank("b"); a.defaultRank = b.defaultRank = true;
  Files.writeString(duplicate.resolve("ranks.json"), GSON.toJson(Map.of("a", a, "b", b)));
  fails(() -> RankStore.load(duplicate), "duplicate defaults rejected");
  Path dangling = temp(); Files.writeString(dangling.resolve("ranks.json"), GSON.toJson(Map.of("vip", manual)));
  Files.writeString(dangling.resolve("players.json"), GSON.toJson(Map.of(PLAYER, "missing")));
  fails(() -> RankStore.load(dangling), "dangling assignments rejected");
  Path badPlayers = temp(); Files.writeString(badPlayers.resolve("ranks.json"), GSON.toJson(Map.of("vip", manual)));
  Files.writeString(badPlayers.resolve("players.json"), "{");
  fails(() -> RankStore.load(badPlayers), "corrupt assignment file rejected");
  check(Files.readString(badPlayers.resolve("players.json")).equals("{"), "corrupt assignments retained");

  fails(() -> new RankPayloads.Save("null", "", 0).rank(), "null network definition rejected");
  fails(() -> new RankPayloads.Save("{", "", 0).rank(), "malformed network definition rejected");
  List<RankDef> large = new ArrayList<>(); for (int i = 0; i < 1000; i++) large.add(rank("rank_" + i));
  var packets = RankPayloads.pages(large, "rank_500", 42); check(packets.size() > 1, "large list split");
  var assembler = new RankPayloads.Assembler(); List<RankDef> received = null;
  for (var packet : packets) {
   var buf = Unpooled.buffer();
   try { RankPayloads.OpenManager.CODEC.encode(buf, packet); var decoded = RankPayloads.OpenManager.CODEC.decode(buf); received = assembler.accept(decoded); }
   finally { buf.release(); }
  }
  check(received != null && received.size() == 1000 && received.get(500).id.equals("rank_500"), "large list codec roundtrip");
  fails(() -> assembler.accept(packets.get(1)), "out-of-order pages rejected");
  fails(() -> assembler.accept(new RankPayloads.OpenManager("[]", "", 0, 0, Integer.MAX_VALUE)), "oversized page count rejected");
  assembler.accept(packets.get(0));
  fails(() -> assembler.accept(new RankPayloads.OpenManager(packets.get(1).ranksJson(), "changed", 42, 1, packets.size())), "mixed page metadata rejected");
  assembler.clear(); check(assembler.accept(RankPayloads.pages(List.of(), "", 0).get(0)).isEmpty(), "empty list and reset");

  RankDef unicode = rank("unicode"); unicode.separator = "\uD83D\uDE00".repeat(10000);
  List<RankDef> unicodeResult = null;
  for (var packet : RankPayloads.pages(List.of(unicode), "unicode", 10)) {
   var buf = Unpooled.buffer();
   try { RankPayloads.OpenManager.CODEC.encode(buf, packet); unicodeResult = assembler.accept(RankPayloads.OpenManager.CODEC.decode(buf)); }
   finally { buf.release(); }
  }
  check(unicodeResult != null && unicodeResult.getFirst().separator.equals(unicode.separator), "surrogate boundary roundtrip");
  RankDef oversized = rank("oversized"); oversized.label = "X".repeat(RankPayloads.MAX_JSON_CHARS);
  fails(() -> RankPayloads.pages(List.of(oversized), "", 0), "list size budget enforced");
  RankDef dirty = rank("dirty"); dirty.label = "   "; dirty.icon = "unknown"; dirty.priority = 500; dirty.textColor = -1; dirty.separator = "a\nb\u202Ec";
  dirty.sanitize(); check(dirty.label.isEmpty() && dirty.icon == null && dirty.priority == 99 && dirty.textColor == 0xFFFFFF, "sanitize persisted fields");
  check(dirty.separator.equals("abc"), "separator strips controls");
  Locale saved = Locale.getDefault();
  try { Locale.setDefault(Locale.forLanguageTag("tr-TR")); check(RankStore.freeId("VIP", null).equals("vip"), "locale-independent IDs"); check(BadgeMeta.sanitize("vip").equals("VIP"), "locale-independent label"); }
  finally { Locale.setDefault(saved); }
  for (int[] size : new int[][]{{320,240},{342,256},{427,240},{480,270},{1920,1080}}) {
   double scale = MenuGeometry.scale(size[0], size[1]);
   check(MenuGeometry.scissorStart(150, scale) <= 150 * scale && MenuGeometry.scissorEnd(150, scale) >= 150 * scale, "scaled scissor bounds");
   check(420 * scale <= size[0] && 236 * scale <= size[1], "editor fits viewport");
   double input = 170 * scale; check(Math.abs(input / scale - 170) < 0.001, "input/render coordinate agreement");
  }
  check(BadgeMeta.icons().size() == 144, "all icons available");
  System.out.println("PASS: " + assertions + " regression assertions");
 }
}
