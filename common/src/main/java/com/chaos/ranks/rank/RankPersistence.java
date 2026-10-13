package com.chaos.ranks.rank;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** One atomic snapshot holds ranks and assignments, preventing partial renames. */
public final class RankPersistence {
 private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
 private static final Type RANKS = new TypeToken<LinkedHashMap<String, RankDef>>() {}.getType();
 private static final Type PLAYERS = new TypeToken<HashMap<UUID, String>>() {}.getType();
 public record Snapshot(int schema, boolean seeded, long revision, LinkedHashMap<String, RankDef> ranks, HashMap<UUID, String> players) {}
 private RankPersistence() {}

 public static Snapshot load(Path dir) throws IOException {
  Path main = dir.resolve("store.json"), backup = dir.resolve("store.json.bak");
  String raw = readOptional(main);
  if (raw != null || Files.exists(backup)) {
   try { if (raw == null) throw new IOException("Missing store.json"); return decode(raw); }
   catch (Exception damaged) {
    if (damaged instanceof UnsupportedSchemaException) throw (UnsupportedSchemaException) damaged;
    String backupRaw = readOptional(backup);
    if (backupRaw == null) throw new IOException("Invalid store.json and no backup", damaged);
    Snapshot recovered;
    try { recovered = decode(backupRaw); } catch (Exception invalidBackup) { throw new IOException("Both rank snapshots are invalid", invalidBackup); }
    if (raw != null) Files.copy(main, dir.resolve("store.corrupt-" + UUID.randomUUID() + ".json"));
    com.chaos.ranks.ChaosRanks.LOGGER.warn("Recovered rank data from {}. The last change may need to be reapplied.", backup);
    return recovered;
   }
  }
  try {
   String ranksRaw = readOptional(dir.resolve("ranks.json"));
   String playersRaw = readOptional(dir.resolve("players.json"));
   String stateRaw = readOptional(dir.resolve("state.json"));
   if (ranksRaw == null && (playersRaw != null || stateRaw != null)) throw new IOException("Missing legacy ranks.json");
   LinkedHashMap<String, RankDef> ranks = ranksRaw == null ? new LinkedHashMap<>() : GSON.fromJson(ranksRaw, RANKS);
   HashMap<UUID, String> players = playersRaw == null ? new HashMap<>() : GSON.fromJson(playersRaw, PLAYERS);
   boolean seeded = false;
   if (stateRaw != null) {
    JsonObject state = JsonParser.parseString(stateRaw).getAsJsonObject();
    if (state.has("seeded")) {
     if (!state.get("seeded").isJsonPrimitive() || !state.getAsJsonPrimitive("seeded").isBoolean()) throw new IOException("Invalid seeded flag");
     seeded = state.get("seeded").getAsBoolean();
    }
   }
   Snapshot migrated = validate(new Snapshot(1, seeded, 0, ranks, players));
   if (ranksRaw != null) save(dir, migrated, migrated);
   return migrated;
  } catch (Exception e) { throw new IOException("Invalid legacy rank data; original files preserved", e); }
 }
 public static Snapshot decode(String raw) throws IOException {
  try {
   JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
   for (String key : List.of("schema", "seeded", "revision", "ranks", "players")) if (!root.has(key) || root.get(key).isJsonNull()) throw new IOException("Missing " + key);
   if (!root.get("schema").isJsonPrimitive() || !root.getAsJsonPrimitive("schema").isNumber() || !root.get("schema").getAsString().equals("1")) throw new UnsupportedSchemaException();
   if (!root.get("revision").isJsonPrimitive() || !root.getAsJsonPrimitive("revision").isNumber() || !root.get("revision").getAsString().matches("[0-9]+")) throw new IOException("Invalid revision");
   if (!root.getAsJsonPrimitive("seeded").isBoolean()) throw new IOException("Invalid seeded flag");
   return validate(GSON.fromJson(root, Snapshot.class));
  } catch (UnsupportedSchemaException e) { throw e; }
  catch (Exception e) { throw new IOException("Invalid rank snapshot", e); }
 }
 public static Snapshot validate(Snapshot state) throws IOException {
  if (state == null || state.schema() != 1 || state.revision() < 0 || state.ranks() == null || state.players() == null) throw new IOException("Invalid snapshot structure");
  boolean defaultSeen = false, opSeen = false;
  for (var entry : state.ranks().entrySet()) {
   validateId(entry.getKey()); RankDef def = entry.getValue();
   if (def == null || !entry.getKey().equals(def.id)) throw new IOException("Invalid rank " + entry.getKey());
   def.sanitize();
   if (def.label.isBlank() && def.icon == null) throw new IOException("Empty rank " + def.id);
   if ((def.defaultRank && defaultSeen) || (def.opRank && opSeen)) throw new IOException("Duplicate default or OP rank");
   defaultSeen |= def.defaultRank; opSeen |= def.opRank;
  }
  for (var entry : state.players().entrySet()) if (entry.getKey() == null || entry.getValue() == null || !state.ranks().containsKey(entry.getValue())) throw new IOException("Unknown assigned rank");
  return state;
 }
 public static void validateId(String id) {
  if (id == null || !id.matches("[a-z0-9_]{1,64}")) throw new IllegalArgumentException("Invalid rank id");
 }
 public static void save(Path dir, Snapshot previous, Snapshot candidate) throws IOException {
  validate(previous); validate(candidate); Files.createDirectories(dir);
  atomicWrite(dir.resolve("store.json.bak"), GSON.toJson(previous));
  atomicWrite(dir.resolve("store.json"), GSON.toJson(candidate));
 }
 private static final class UnsupportedSchemaException extends IOException {
  UnsupportedSchemaException() { super("Unsupported rank snapshot schema; refusing backup rollback"); }
 }
 private static String readOptional(Path path) throws IOException {
  try { return Files.readString(path, StandardCharsets.UTF_8); }
  catch (NoSuchFileException e) { return null; }
 }
 private static void atomicWrite(Path target, String text) throws IOException {
  Path temp = Files.createTempFile(target.getParent(), ".rank-", ".tmp");
  try {
   try (FileChannel channel = FileChannel.open(temp, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
    ByteBuffer bytes = StandardCharsets.UTF_8.encode(text);
    while (bytes.hasRemaining()) channel.write(bytes);
    channel.force(true);
   }
   // Fail safely if this filesystem cannot provide an atomic replacement.
   Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
  } finally { Files.deleteIfExists(temp); }
 }
}
