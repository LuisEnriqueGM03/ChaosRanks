package com.chaos.ranks.rank;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.chaos.ranks.ChaosRanks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Rangos y asignaciones, en <mundo>/chaosranks/{ranks,players,state}.json.
 * El rango que se ve es: el asignado a mano → si es OP, el rango de OP → el rango por defecto.
 */
public final class RankStore {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type RANKS_TYPE = new TypeToken<LinkedHashMap<String, RankDef>>() {}.getType();
	private static final Type PLAYERS_TYPE = new TypeToken<HashMap<UUID, String>>() {}.getType();

	private static Map<String, RankDef> ranks = new LinkedHashMap<>();
	private static Map<UUID, String> players = new HashMap<>();
	private static Path dir;

	private RankStore() {}

	public static void load(MinecraftServer server) {
		dir = server.getWorldPath(LevelResource.ROOT).resolve(ChaosRanks.MOD_ID);
		Map<String, RankDef> r = read(dir.resolve("ranks.json"), RANKS_TYPE);
		Map<UUID, String> p = read(dir.resolve("players.json"), PLAYERS_TYPE);
		ranks = r != null ? r : new LinkedHashMap<>();
		players = p != null ? p : new HashMap<>();

		// La primera vez en cada mundo se crean PLAYER (por defecto) y OP (para los OP).
		JsonObject state = read(dir.resolve("state.json"), JsonObject.class);
		if (state == null || !state.has("seeded")) {
			seedDefaults();
			state = new JsonObject();
			state.addProperty("seeded", true);
			write("state.json", GSON.toJson(state));
			save();
		}
		ChaosRanks.LOGGER.info("{} rangos, {} jugadores con rango asignado", ranks.size(), players.size());
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
		return ranks.values();
	}

	@Nullable
	public static RankDef get(String id) {
		return ranks.get(id);
	}

	/** Rango que se ve para este jugador. */
	@Nullable
	public static RankDef rankOf(ServerPlayer player) {
		String id = players.get(player.getUUID());
		RankDef assigned = id != null ? ranks.get(id) : null;
		if (assigned != null) {
			return assigned;
		}
		if (player.server.getPlayerList().isOp(player.getGameProfile())) {
			RankDef op = find(d -> d.opRank);
			if (op != null) {
				return op;
			}
		}
		return find(d -> d.defaultRank);
	}

	@Nullable
	private static RankDef find(Predicate<RankDef> test) {
		return ranks.values().stream().filter(test).findFirst().orElse(null);
	}

	/**
	 * Guarda un rango. Si es uno existente y cambia de id (se le cambió el texto), los jugadores que lo tenían
	 * pasan al id nuevo. "Por defecto" y "para OP" solo los puede llevar un rango.
	 */
	public static void put(@Nullable String originalId, RankDef def) {
		if (originalId != null && !originalId.equals(def.id)) {
			ranks.remove(originalId);
			players.replaceAll((uuid, rank) -> rank.equals(originalId) ? def.id : rank);
		}
		if (def.defaultRank) {
			ranks.values().forEach(d -> d.defaultRank = false);
		}
		if (def.opRank) {
			ranks.values().forEach(d -> d.opRank = false);
		}
		ranks.put(def.id, def);
		save();
	}

	public static void put(RankDef def) {
		put(null, def);
	}

	public static void delete(String id) {
		ranks.remove(id);
		players.values().removeIf(id::equals);
		save();
	}

	public static void assign(UUID player, @Nullable String rankId) {
		if (rankId == null) {
			players.remove(player);
		} else {
			players.put(player, rankId);
		}
		save();
	}

	/** Id libre a partir del texto: "VIP+" -> "vip", "vip_2"... {@code keep} cuenta como libre (el propio rango). */
	public static String freeId(String label, @Nullable String keep) {
		String base = label.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
		if (base.isEmpty()) {
			base = "rank";
		}
		String id = base;
		for (int i = 2; ranks.containsKey(id) && !id.equals(keep); i++) {
			id = base + "_" + i;
		}
		return id;
	}

	private static void save() {
		write("ranks.json", GSON.toJson(ranks, RANKS_TYPE));
		write("players.json", GSON.toJson(players, PLAYERS_TYPE));
	}

	private static void write(String file, String json) {
		if (dir == null) {
			return;
		}
		try {
			Files.createDirectories(dir);
			Files.writeString(dir.resolve(file), json, StandardCharsets.UTF_8);
		} catch (IOException e) {
			ChaosRanks.LOGGER.error("No se pudo guardar {}", file, e);
		}
	}

	@Nullable
	private static <T> T read(Path file, Type type) {
		if (!Files.exists(file)) {
			return null;
		}
		try {
			return GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), type);
		} catch (Exception e) {
			ChaosRanks.LOGGER.error("No se pudo leer {}", file, e);
			return null;
		}
	}
}
