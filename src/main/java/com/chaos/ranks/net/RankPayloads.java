package com.chaos.ranks.net;

import java.util.*;
import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.rank.RankDef;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Versioned, bounded editor messages; large rank lists are split before encoding. */
public final class RankPayloads {
 private static final Gson GSON = new Gson();
 public static final int CHUNK_CHARS = 16000;
 public static final int MAX_JSON_CHARS = 4 * 1024 * 1024;
 private RankPayloads() {}
 public record OpenManager(String ranksJson, String selectId, long revision, int page, int pages) implements CustomPacketPayload {
  public static final Type<OpenManager> TYPE = new Type<>(ChaosRanks.id("open_manager_v2"));
  public static final StreamCodec<ByteBuf, OpenManager> CODEC = StreamCodec.composite(
   ByteBufCodecs.stringUtf8(CHUNK_CHARS), OpenManager::ranksJson,
   ByteBufCodecs.stringUtf8(64), OpenManager::selectId,
   ByteBufCodecs.VAR_LONG, OpenManager::revision,
   ByteBufCodecs.VAR_INT, OpenManager::page,
   ByteBufCodecs.VAR_INT, OpenManager::pages, OpenManager::new);
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
 }
 public static List<OpenManager> pages(List<RankDef> ranks, String selectId, long revision) {
  String json = GSON.toJson(ranks);
  if (json.length() > MAX_JSON_CHARS) throw new IllegalArgumentException("Rank list too large");
  List<String> chunks = new ArrayList<>();
  for (int start = 0; start < json.length();) {
   int end = Math.min(json.length(), start + CHUNK_CHARS);
   if (end < json.length() && Character.isHighSurrogate(json.charAt(end - 1))) end--;
   chunks.add(json.substring(start, end)); start = end;
  }
  List<OpenManager> packets = new ArrayList<>();
  for (int i = 0; i < chunks.size(); i++) packets.add(new OpenManager(chunks.get(i), selectId, revision, i, chunks.size()));
  return packets;
 }
 /** One assembler per connection. Out-of-order and oversized responses cannot open a partial editor. */
 public static final class Assembler {
  private final StringBuilder json = new StringBuilder();
  private int next, pages;
  private long revision;
  private String selectId = "";
  public void clear() { json.setLength(0); next = 0; pages = 0; }
  public List<RankDef> accept(OpenManager packet) {
   if (packet.pages() < 1 || packet.pages() > MAX_JSON_CHARS / (CHUNK_CHARS - 1) + 1 || packet.page() < 0 || packet.page() >= packet.pages()) { clear(); throw new IllegalArgumentException("Invalid rank page"); }
   if (packet.page() == 0) { clear(); pages = packet.pages(); revision = packet.revision(); selectId = packet.selectId(); }
   if (packet.page() != next || packet.pages() != pages || packet.revision() != revision || !packet.selectId().equals(selectId) || json.length() + packet.ranksJson().length() > MAX_JSON_CHARS) { clear(); throw new IllegalArgumentException("Inconsistent rank pages"); }
   json.append(packet.ranksJson()); next++;
   if (next != pages) return null;
   try {
    List<RankDef> ranks = GSON.fromJson(json.toString(), new TypeToken<List<RankDef>>() {}.getType());
    if (ranks == null || ranks.stream().anyMatch(d -> d == null || d.id == null || d.label == null || d.separator == null)) throw new IllegalArgumentException("Invalid rank list");
    return ranks;
   } finally { clear(); }
  }
 }
 public record Save(String rankJson, String originalId, long revision) implements CustomPacketPayload {
  public static final Type<Save> TYPE = new Type<>(ChaosRanks.id("save_rank_v2"));
  public static final StreamCodec<ByteBuf, Save> CODEC = StreamCodec.composite(
   ByteBufCodecs.stringUtf8(4096), Save::rankJson,
   ByteBufCodecs.stringUtf8(64), Save::originalId,
   ByteBufCodecs.VAR_LONG, Save::revision, Save::new);
  public Save(RankDef def, String originalId, long revision) { this(GSON.toJson(def), originalId, revision); }
  public RankDef rank() { return Objects.requireNonNull(GSON.fromJson(rankJson, RankDef.class), "Null rank"); }
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
 }
 public record Delete(String id, long revision) implements CustomPacketPayload {
  public static final Type<Delete> TYPE = new Type<>(ChaosRanks.id("delete_rank_v2"));
  public static final StreamCodec<ByteBuf, Delete> CODEC = StreamCodec.composite(
   ByteBufCodecs.stringUtf8(64), Delete::id,
   ByteBufCodecs.VAR_LONG, Delete::revision, Delete::new);
  @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
 }
}
