package com.chaos.ranks.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.chaos.ranks.badge.BadgeRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibilidad con mods de chat que cancelan el chat vanilla y reenvían el mensaje como mensaje de sistema con
 * el nombre en texto plano (NeoEssentials con enable-chat-formatting): ahí no pasa por getDisplayName() y se
 * pierden la etiqueta y el color del nombre.
 * Cuando un jugador acaba de escribir en el chat, la primera vez que su nombre aparece en un mensaje de sistema se
 * cambia por su nombre con rango (prefijo del equipo + estilo, ver PlayerTeamMixin).
 * En vanilla no hace nada: los mensajes de chat no son mensajes de sistema.
 */
public final class ChatNameRewriter {

	/** El mod de chat puede reenviarlo un tick después; con esto sobra. */
	private static final long WINDOW_NANOS = 2_000_000_000L;

	private record Pending(ServerPlayer player, long time) {}

	// handleChat corre en el hilo de red; el envío, en el del servidor.
	private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

	// El mismo mensaje se manda a cada jugador: se reescribe una vez.
	private static Component lastIn;
	private static Component lastOut;

	private ChatNameRewriter() {}

	public static void chatReceived(ServerPlayer player) {
		PENDING.put(player.getUUID(), new Pending(player, System.nanoTime()));
	}

	public static Component rewrite(Component message) {
		if (PENDING.isEmpty() || message == null) {
			return message;
		}
		if (message == lastIn) {
			return lastOut;
		}
		Component out = message;
		long now = System.nanoTime();
		String plain = null;
		for (Pending pending : PENDING.values()) {
			if (now - pending.time > WINDOW_NANOS || pending.player.hasDisconnected()) {
				PENDING.remove(pending.player.getUUID(), pending);
				continue;
			}
			if (plain == null) {
				plain = message.getString();
			}
			String name = pending.player.getGameProfile().getName();
			if (wordIndex(plain, name, 0) < 0 || hasBadge(message)) {
				continue;
			}
			boolean[] done = {false};
			out = replace(message, name, pending.player.getDisplayName(), done);
			break;
		}
		lastIn = message;
		lastOut = out;
		return out;
	}

	private static MutableComponent replace(Component c, String name, Component display, boolean[] done) {
		MutableComponent out;
		int i;
		if (!done[0] && c.getContents() instanceof PlainTextContents text && (i = wordIndex(text.text(), name, 0)) >= 0) {
			done[0] = true;
			String before = text.text().substring(0, i);
			String after = text.text().substring(i + name.length());
			out = Component.empty().setStyle(c.getStyle());
			if (!before.isEmpty()) {
				out.append(Component.literal(before));
			}
			out.append(display);
			if (!after.isEmpty()) {
				// Los códigos § no pasan de un componente a otro: se repiten los que había antes del nombre.
				out.append(Component.literal(legacyState(before) + after));
			}
		} else {
			out = c.plainCopy().setStyle(c.getStyle());
		}
		for (Component sibling : c.getSiblings()) {
			out.append(replace(sibling, name, display, done));
		}
		return out;
	}

	/** Posición del nombre como palabra entera (los nombres de Minecraft son [A-Za-z0-9_]). */
	private static int wordIndex(String s, String name, int from) {
		for (int i = s.indexOf(name, from); i >= 0; i = s.indexOf(name, i + 1)) {
			int end = i + name.length();
			boolean start = i == 0 || !isNameChar(s.charAt(i - 1)) || (i >= 2 && s.charAt(i - 2) == '§');
			if (start && (end == s.length() || !isNameChar(s.charAt(end)))) {
				return i;
			}
		}
		return -1;
	}

	private static boolean isNameChar(char c) {
		return c == '_' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	/** Códigos § activos al final de s: el último color y los formatos que le siguen. */
	private static String legacyState(String s) {
		StringBuilder state = new StringBuilder();
		for (int i = 0; i < s.length() - 1; i++) {
			if (s.charAt(i) != '§') {
				continue;
			}
			char code = Character.toLowerCase(s.charAt(++i));
			if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f') || code == 'r') {
				state.setLength(0);
			}
			if (code != 'r') {
				state.append('§').append(code);
			}
		}
		return state.toString();
	}

	/** Si ya lleva una etiqueta (el mod usó getDisplayName()), no se toca. */
	private static boolean hasBadge(Component c) {
		if (BadgeRenderer.FONT.equals(c.getStyle().getFont())) {
			return true;
		}
		for (Component sibling : c.getSiblings()) {
			if (hasBadge(sibling)) {
				return true;
			}
		}
		return false;
	}
}
