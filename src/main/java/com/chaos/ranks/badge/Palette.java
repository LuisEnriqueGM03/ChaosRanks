package com.chaos.ranks.badge;

import java.util.List;

import net.minecraft.network.chat.Component;

/** Colores que se pueden elegir en el menú. El nombre se traduce con chaosranks.color.<id>. */
public final class Palette {

	public record Color(String id, int rgb) {

		public Component name() {
			return Component.translatable("chaosranks.color." + id);
		}
	}

	public static final List<Color> COLORS = List.of(
			new Color("red", 0xFF3B3B),
			new Color("dark_red", 0xB3131B),
			new Color("orange", 0xFF8A1F),
			new Color("copper", 0xD9773F),
			new Color("gold", 0xFFC300),
			new Color("yellow", 0xFFE14D),
			new Color("lime", 0x7CE01A),
			new Color("emerald", 0x17C45A),
			new Color("green", 0x3C8A2E),
			new Color("turquoise", 0x22D3C5),
			new Color("diamond", 0x3FD8EA),
			new Color("cyan", 0x1599B0),
			new Color("light_blue", 0x4AA3FF),
			new Color("blue", 0x2F5BFF),
			new Color("lapis", 0x2440A8),
			new Color("purple", 0x9B36E8),
			new Color("amethyst", 0xB57CF0),
			new Color("magenta", 0xE23BD9),
			new Color("pink", 0xFF6FB5),
			new Color("brown", 0x8B5A2F),
			new Color("white", 0xFFFFFF),
			new Color("light_gray", 0xC0C0C0),
			new Color("gray", 0x7A7A7A),
			new Color("obsidian", 0x3B2A5C),
			new Color("black", 0x1E1E1E),
			new Color("youtube", 0xFF0000),
			new Color("twitch", 0x9146FF),
			new Color("discord", 0x5865F2));

	private Palette() {}
}
