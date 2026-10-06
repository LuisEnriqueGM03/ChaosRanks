package com.chaos.ranks.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.chaos.ranks.badge.BadgeRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

/**
 * Un equipo del scoreboard por rango, con la etiqueta como prefijo.
 * Vanilla ya pinta el prefijo en el chat, en el Tab y encima de la cabeza, y ordena el Tab por nombre de equipo.
 */
public final class RankTeams {

	public static final String PREFIX = "chaosrank_";

	private RankTeams() {}

	public static void sync(MinecraftServer server) {
		sync(server, null);
	}

	/**
	 * @param joining jugador que está entrando: el evento de entrada llega antes de que esté en la lista de
	 *                jugadores del servidor, así que se añade a mano.
	 */
	public static void sync(MinecraftServer server, @Nullable ServerPlayer joining) {
		ServerScoreboard board = server.getScoreboard();

		Map<String, PlayerTeam> wanted = new HashMap<>();
		for (RankDef def : RankStore.ranks()) {
			String name = teamName(def);
			PlayerTeam team = board.getPlayerTeam(name);
			if (team == null) {
				team = board.addPlayerTeam(name);
			}
			// El estilo del nombre del equipo es el del nombre del jugador (ver PlayerTeamMixin).
			team.setDisplayName(Component.literal(def.label).setStyle(def.nameStyle()));
			team.setPlayerPrefix(BadgeRenderer.build(def).append(Component.literal(" ")));
			wanted.put(def.id, team);
		}

		for (PlayerTeam team : List.copyOf(board.getPlayerTeams())) {
			if (team.getName().startsWith(PREFIX) && !wanted.containsValue(team)) {
				board.removePlayerTeam(team);
			}
		}

		List<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
		if (joining != null && !players.contains(joining)) {
			players.add(joining);
		}
		for (ServerPlayer player : players) {
			String name = player.getScoreboardName();
			RankDef def = RankStore.rankOf(player);
			PlayerTeam current = board.getPlayersTeam(name);
			PlayerTeam target = def != null ? wanted.get(def.id) : null;
			if (target != null) {
				if (current != target) {
					board.addPlayerToTeam(name, target);
				}
			} else if (current != null && current.getName().startsWith(PREFIX)) {
				board.removePlayerFromTeam(name, current);
			}
		}
	}

	/** El Tab ordena por nombre de equipo: la prioridad invertida va delante. */
	private static String teamName(RankDef def) {
		int order = RankDef.MAX_PRIORITY - Math.max(0, Math.min(RankDef.MAX_PRIORITY, def.priority));
		return PREFIX + String.format("%02d", order) + "_" + def.id;
	}
}
