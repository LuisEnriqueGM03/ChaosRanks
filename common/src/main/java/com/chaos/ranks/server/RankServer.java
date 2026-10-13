package com.chaos.ranks.server;

import java.util.ArrayList;
import java.util.List;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.Lang;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;

import com.chaos.ranks.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

/** Lado servidor del menú: abrirlo y aplicar lo que el admin guarda o borra desde el cliente. */
public final class RankServer {

	private static final int PERMISSION = 2;

	private RankServer() {}


	public static void open(ServerPlayer player, String selectId) {
  if (!player.hasPermissions(PERMISSION)) return;
  if (!RankStore.writable()) { error(player, "msg.storage_error"); return; }
		if (!Services.PLATFORM.canSend(player)) {
			player.sendSystemMessage(Lang.tr("msg.need_client")
					.withStyle(ChatFormatting.RED));
			return;
		}
		List<RankDef> ranks = new ArrayList<>(RankStore.ranks());
		ranks.sort((a, b) -> Integer.compare(b.priority, a.priority));
		try {
   for (var page : RankPayloads.pages(ranks, selectId, RankStore.revision())) Services.PLATFORM.sendToPlayer(player, page);
  } catch (IllegalArgumentException e) { error(player, "msg.too_many_ranks"); }
	}

	public static void save(ServerPlayer player, RankPayloads.Save payload) {
		if (!player.hasPermissions(PERMISSION)) {
			return;
		}
		if (!checkRevision(player, payload.revision())) return;
		RankDef def;
		try {
			def = payload.rank();
   def.sanitize();
		} catch (Exception e) {
			ChaosRanks.LOGGER.warn("Rango inválido de {}", player.getScoreboardName(), e);
   error(player, "msg.invalid_rank");
			return;
		}
		if (def.label.isBlank() && def.icon == null) {
			player.sendSystemMessage(Lang.tr("msg.empty_badge").withStyle(ChatFormatting.RED));
			return;
		}
		// El id sigue al texto: si se renombra, los jugadores que lo tenían pasan al id nuevo.
		String original = payload.originalId().isEmpty() ? null : payload.originalId();
  if (original != null && RankStore.get(original) == null) { error(player, "msg.stale_editor"); return; }
		def.id = RankStore.freeId(def.label.isEmpty() ? def.icon : def.label, original);
		try { RankStore.put(original, def); }
  catch (RankStore.StorageException e) { ChaosRanks.LOGGER.error("Rank save failed", e); error(player, "msg.storage_error"); return; }
		RankTeams.sync(player.server);
		open(player, def.id);
	}

 private static void error(ServerPlayer player, String key) { player.sendSystemMessage(Lang.tr(key).withStyle(ChatFormatting.RED)); }
 private static boolean checkRevision(ServerPlayer player, long expected) {
  if (!RankStore.writable()) { error(player, "msg.storage_error"); return false; }
  try { RankStore.requireRevision(expected); return true; }
  catch (IllegalStateException e) { error(player, "msg.stale_editor"); return false; }
 }

	public static void delete(ServerPlayer player, RankPayloads.Delete payload) {
  String id = payload.id();
		if (!player.hasPermissions(PERMISSION) || !checkRevision(player, payload.revision()) || RankStore.get(id) == null) {
			return;
		}
		try { RankStore.delete(id); }
  catch (RankStore.StorageException e) { ChaosRanks.LOGGER.error("Rank delete failed", e); error(player, "msg.storage_error"); return; }
		RankTeams.sync(player.server);
		open(player, "");
	}
}
