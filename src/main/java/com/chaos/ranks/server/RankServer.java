package com.chaos.ranks.server;

import java.util.ArrayList;
import java.util.List;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.Lang;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/** Lado servidor del menú: abrirlo y aplicar lo que el admin guarda o borra desde el cliente. */
public final class RankServer {

	private static final int PERMISSION = 2;

	private RankServer() {}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Save.TYPE, (payload, context) ->
				context.server().execute(() -> save(context.player(), payload)));
		ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Delete.TYPE, (payload, context) ->
				context.server().execute(() -> delete(context.player(), payload.id())));
	}

	public static void open(ServerPlayer player, String selectId) {
		if (!ServerPlayNetworking.canSend(player, RankPayloads.OpenManager.TYPE)) {
			player.sendSystemMessage(Lang.tr("msg.need_client")
					.withStyle(ChatFormatting.RED));
			return;
		}
		List<RankDef> ranks = new ArrayList<>(RankStore.ranks());
		ranks.sort((a, b) -> Integer.compare(b.priority, a.priority));
		ServerPlayNetworking.send(player, new RankPayloads.OpenManager(ranks, selectId));
	}

	private static void save(ServerPlayer player, RankPayloads.Save payload) {
		if (!player.hasPermissions(PERMISSION)) {
			return;
		}
		RankDef def;
		try {
			def = payload.rank();
		} catch (Exception e) {
			ChaosRanks.LOGGER.warn("Rango inválido de {}", player.getScoreboardName(), e);
			return;
		}
		def.sanitize();
		if (def.label.isEmpty() && def.icon == null) {
			player.sendSystemMessage(Lang.tr("msg.empty_badge").withStyle(ChatFormatting.RED));
			return;
		}
		// El id sigue al texto: si se renombra, los jugadores que lo tenían pasan al id nuevo.
		String original = !payload.originalId().isEmpty() && RankStore.get(payload.originalId()) != null
				? payload.originalId()
				: null;
		def.id = RankStore.freeId(def.label.isEmpty() ? def.icon : def.label, original);
		RankStore.put(original, def);
		RankTeams.sync(player.server);
		open(player, def.id);
	}

	private static void delete(ServerPlayer player, String id) {
		if (!player.hasPermissions(PERMISSION) || RankStore.get(id) == null) {
			return;
		}
		RankStore.delete(id);
		RankTeams.sync(player.server);
		open(player, "");
	}
}
