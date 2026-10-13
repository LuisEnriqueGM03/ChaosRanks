package com.chaos.ranks.server;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.rank.RankStore;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;

/**
 * Formato del chat: "ETIQUETA Nombre » mensaje", sin los "<>" de vanilla.
 * El tipo de chat chaosranks:rank (data/chaosranks/chat_type/rank.json) es solo "%s %s"; el separador y el color
 * del mensaje se añaden al contenido con el decorador, así cada rango tiene los suyos.
 */
public final class RankChat {

	public static final ResourceKey<ChatType> CHAT_TYPE = ResourceKey.create(Registries.CHAT_TYPE, ChaosRanks.id("rank"));

	private static final int DEFAULT_SEPARATOR_COLOR = 0xAAAAAA;

	private RankChat() {}

 public static Component decorate(ServerPlayer sender, Component message) {
  RankDef def = sender != null ? RankStore.rankOf(sender) : null;
  return def != null ? format(def.separator, def.separatorColor, def.messageColor, message)
   : format(RankDef.DEFAULT_SEPARATOR, DEFAULT_SEPARATOR_COLOR, 0xFFFFFF, message);
 }

	/** Lo que va detrás del nombre. También lo usa la vista previa del menú. */
	public static MutableComponent format(String separator, int separatorColor, int messageColor, Component message) {
		MutableComponent out = Component.empty();
		if (separator != null && !separator.isEmpty()) {
			out.append(Component.literal(separator + " ").withColor(separatorColor));
		}
		return out.append(Component.empty().withColor(messageColor).append(message));
	}
}
