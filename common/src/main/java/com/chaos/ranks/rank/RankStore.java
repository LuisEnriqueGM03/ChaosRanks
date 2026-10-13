package com.chaos.ranks.rank;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;
import com.chaos.ranks.ChaosRanks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

/** Transactional rank state. Mutations are published only after durable persistence. */
public final class RankStore {
 private static Map<String, RankDef> ranks = new LinkedHashMap<>();
 private static Map<UUID, String> players = new HashMap<>();
 private static Path dir;
 private static long revision;
 private static boolean writable;
 private RankStore() {}

 public static void load(MinecraftServer server) {
  try { load(server.getWorldPath(LevelResource.ROOT).resolve(ChaosRanks.MOD_ID)); }
  catch (StorageException e) { ChaosRanks.LOGGER.error("Rank data unavailable; edits disabled. Repair the files and restart.", e); }
 }

 public static void load(Path path) {
  dir = path; writable = false; ranks = new LinkedHashMap<>(); players = new HashMap<>(); revision = 0;
  try {
   RankPersistence.Snapshot state = RankPersistence.load(path);
   ranks = state.ranks(); players = state.players(); revision = state.revision();
   writable = true;
   if (!state.seeded()) { seedDefaults(); persist(ranks, players); }
  } catch (Exception e) {
   writable = false;
   throw new StorageException(e);
  }
 }
 public static long revision() { return revision; }
 public static boolean writable() { return writable; }
 public static void requireRevision(long expected) {
  if (expected != revision) throw new IllegalStateException("stale rank editor");
 }
	private static void seedDefaults() {
		if (find(d -> d.defaultRank) == null) {
			RankDef player = new RankDef();
			player.id = freeId("player", null);
			player.label = "PLAYER";
			player.icon = "person";
			player.bgColor = 0x7A7A7A;
			player.priority = 1;
			player.nameRgb = 0xDDDDDD;
			player.defaultRank = true;
			ranks.put(player.id, player);
		}
		if (find(d -> d.opRank) == null) {
			RankDef op = new RankDef();
			op.id = freeId("op", null);
			op.label = "OP";
			op.icon = "crown";
			op.bgColor = 0xFFC300;
			op.priority = 99;
			op.nameRgb = 0xFFC300;
			op.nameBold = true;
			op.separator = "->";
			op.separatorColor = 0x8A8A8A;
			op.messageColor = 0xFFE14D;
			op.opRank = true;
			ranks.put(op.id, op);
		}
	}


 public static Collection<RankDef> ranks() {
  return ranks.values().stream().map(RankDef::copy).toList();
 }
 @Nullable public static RankDef get(String id) {
  RankDef def = ranks.get(id); return def == null ? null : def.copy();
 }
 @Nullable public static RankDef rankOf(ServerPlayer player) {
  RankDef assigned = ranks.get(players.get(player.getUUID()));
  if (assigned != null) return assigned.copy();
  if (player.server.getPlayerList().isOp(player.getGameProfile())) {
   RankDef op = find(d -> d.opRank); if (op != null) return op.copy();
  }
  RankDef fallback = find(d -> d.defaultRank); return fallback == null ? null : fallback.copy();
 }
 @Nullable private static RankDef find(Predicate<RankDef> predicate) {
  return ranks.values().stream().filter(predicate).findFirst().orElse(null);
 }
 public static void put(@Nullable String originalId, RankDef input) {
  RankDef def = Objects.requireNonNull(input).copy(); def.sanitize();
  RankPersistence.validateId(def.id);
  if (def.label.isBlank() && def.icon == null) throw new IllegalArgumentException("empty badge");
  if (originalId != null && !ranks.containsKey(originalId)) throw new IllegalStateException("deleted rank");
  if (!Objects.equals(originalId, def.id) && ranks.containsKey(def.id)) throw new IllegalArgumentException("duplicate rank id");
  Map<String, RankDef> next = copyRanks(); Map<UUID, String> assignments = new HashMap<>(players);
  if (originalId != null && !originalId.equals(def.id)) {
   next.remove(originalId); assignments.replaceAll((uuid, rank) -> rank.equals(originalId) ? def.id : rank);
  }
  if (def.defaultRank) next.values().forEach(d -> d.defaultRank = false);
  if (def.opRank) next.values().forEach(d -> d.opRank = false);
  next.put(def.id, def); persist(next, assignments);
 }
 public static void put(RankDef def) { put(ranks.containsKey(def.id) ? def.id : null, def); }
 public static void delete(String id) {
  Map<String, RankDef> next = copyRanks(); Map<UUID, String> assignments = new HashMap<>(players);
  next.remove(id); assignments.values().removeIf(id::equals); persist(next, assignments);
 }
 public static void assign(UUID player, @Nullable String id) { assignAll(List.of(player), id); }
 public static void assignAll(Collection<UUID> targets, @Nullable String id) {
  if (id != null && !ranks.containsKey(id)) throw new IllegalArgumentException("unknown rank");
  Map<UUID, String> assignments = new HashMap<>(players);
  for (UUID uuid : targets) { Objects.requireNonNull(uuid); if (id == null) assignments.remove(uuid); else assignments.put(uuid, id); }
  persist(copyRanks(), assignments);
 }
 public static String freeId(String label, @Nullable String keep) {
  String base = label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
  if (base.isEmpty()) base = "rank";
  base = base.substring(0, Math.min(48, base.length()));
  String id = base;
  for (int i = 2; ranks.containsKey(id) && !id.equals(keep); i++) id = base + "_" + i;
  return id;
 }
 private static Map<String, RankDef> copyRanks() {
  Map<String, RankDef> copy = new LinkedHashMap<>(); ranks.forEach((id, def) -> copy.put(id, def.copy())); return copy;
 }
 private static void persist(Map<String, RankDef> next, Map<UUID, String> assignments) {
  if (!writable || dir == null) throw new StorageException(new IllegalStateException("rank storage is read-only"));
  var previous = new RankPersistence.Snapshot(1, true, revision, new LinkedHashMap<>(ranks), new HashMap<>(players));
  var candidate = new RankPersistence.Snapshot(1, true, revision + 1, new LinkedHashMap<>(next), new HashMap<>(assignments));
  try { RankPersistence.save(dir, previous, candidate); }
  catch (Exception e) { throw new StorageException(e); }
  ranks = next; players = assignments; revision = candidate.revision();
 }
 public static final class StorageException extends RuntimeException {
  public StorageException(Throwable cause) { super("Could not load or save rank data", cause); }
 }
}
