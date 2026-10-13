package com.chaos.ranks.badge;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.rank.RankDef;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Monta la etiqueta como texto con la fuente chaosranks:badge, en tres capas que se dibujan una encima de otra
 * retrocediendo con espacios negativos: borde → fondo interior → icono y letras.
 * Las capas no comparten ni un píxel (el fondo lleva el hueco de cada letra), así que no parpadean en el mundo 3D.
 * Al ser un Component normal funciona igual en el chat, el Tab, el nametag, los objetos y los títulos de menús.
 */
public final class BadgeRenderer {

	public static final net.minecraft.network.chat.FontDescription FONT = new net.minecraft.network.chat.FontDescription.Resource(ChaosRanks.id("badge"));
	/** Misma fuente (mismos codepoints y anchos) en 9 px de alto, para el Tab. */
	public static final net.minecraft.network.chat.FontDescription FONT_COMPACT = new net.minecraft.network.chat.FontDescription.Resource(ChaosRanks.id("badge_compact"));

	// Codepoints de assets/chaosranks/font/badge.json (ver tools/gen_font.py).
	private static final int RING_FILL = 0xE000;
	private static final char RING_CAP = '';
	private static final int INNER_FILL = 0xE010;
	private static final int NEG_SPACE = 0xE020;
	private static final int POS_SPACE = 0xE030;
	private static final char NEG_1 = (char) NEG_SPACE;
	private static final int MAX_FILL_BIT = 5;   // piezas de 1, 2, 4 ... 32
	private static final int MAX_SPACE_BIT = 7;  // espacios de 1, 2, 4 ... 128

	private static final int PAD = 2;
	private static final int ICON_GAP = 2;

	private BadgeRenderer() {}

	public static MutableComponent build(RankDef def) {
		String label = BadgeMeta.sanitize(def.label);
		BadgeMeta.Icon icon = BadgeMeta.icon(def.icon);

		int labelPx = 0;
		for (char c : label.toCharArray()) {
			labelPx += advance(c, def.bold);
		}
		if (!label.isEmpty()) {
			labelPx--; // sin la separación tras la última letra
		}
		int iconPx = icon == null ? 0 : icon.width() + (label.isEmpty() ? 0 : ICON_GAP);
		int total = 1 + PAD + iconPx + labelPx + PAD + 1;

		MutableComponent out = Component.empty();

		// 1. Borde de 1 px con las esquinas recortadas: color del marco, o del fondo si no hay marco.
		StringBuilder ring = new StringBuilder();
		ring.append(RING_CAP).append(NEG_1);
		fill(ring, RING_FILL, total - 2);
		ring.append(RING_CAP).append(NEG_1);
		ring.append(space(-total));
		out.append(seg(ring, def.frameColor != null ? def.frameColor : def.bgColor));

		// 2. Fondo interior, con el hueco de cada letra y del icono.
		StringBuilder bg = new StringBuilder(space(1));
		int x = 1;
		fill(bg, INNER_FILL, PAD);
		x += PAD;
		if (icon != null) {
			bg.append(icon.knockout()).append(NEG_1);
			x += icon.width() + 1;
			if (!label.isEmpty()) {
				fill(bg, INNER_FILL, ICON_GAP - 1);
				x += ICON_GAP - 1;
			}
		}
		for (char c : label.toCharArray()) {
			BadgeMeta.Letter l = BadgeMeta.letter(c);
			if (l == null) {
				fill(bg, INNER_FILL, BadgeMeta.labelSpace());
			} else {
				bg.append(def.bold ? l.boldKnockout() : l.knockout()).append(NEG_1);
			}
			x += advance(c, def.bold);
		}
		fill(bg, INNER_FILL, total - 1 - x);
		bg.append(space(-(total - 1)));
		out.append(seg(bg, def.bgColor));

		// 3. Icono y letras, justo en los huecos.
		x = 1 + PAD;
		out.append(seg(new StringBuilder(space(x)), 0xFFFFFF));
		if (icon != null) {
			StringBuilder s = new StringBuilder().append(icon.glyph());
			x += icon.width() + 1;
			if (!label.isEmpty()) {
				s.append(space(ICON_GAP - 1));
				x += ICON_GAP - 1;
			}
			out.append(seg(s, def.iconColor));
		}
		if (!label.isEmpty()) {
			StringBuilder s = new StringBuilder();
			for (char c : label.toCharArray()) {
				BadgeMeta.Letter l = BadgeMeta.letter(c);
				s.append(l == null ? ' ' : def.bold ? l.boldGlyph() : l.glyph());
				x += advance(c, def.bold);
			}
			out.append(seg(s, def.textColor));
		}
		out.append(seg(new StringBuilder(space(total - x)), 0xFFFFFF));
		return out;
	}

	/** Copia del texto con las etiquetas en la fuente compacta. */
	public static Component compact(Component component) {
		Style style = component.getStyle();
		if (FONT.equals(style.getFont())) {
			style = style.withFont(FONT_COMPACT);
		}
		MutableComponent out = MutableComponent.create(component.getContents()).setStyle(style);
		for (Component sibling : component.getSiblings()) {
			out.append(compact(sibling));
		}
		return out;
	}

	/** Solo el icono, para los selectores. */
	public static MutableComponent icon(BadgeMeta.Icon icon, int color) {
		return seg(new StringBuilder().append(icon.glyph()), color);
	}

	private static int advance(char c, boolean bold) {
		BadgeMeta.Letter l = BadgeMeta.letter(c);
		if (l == null) {
			return BadgeMeta.labelSpace();
		}
		return (bold ? l.boldWidth() : l.width()) + 1;
	}

	private static MutableComponent seg(CharSequence text, int color) {
		return Component.literal(text.toString()).setStyle(Style.EMPTY.withFont(FONT).withColor(color).withItalic(false).withShadowColor(0));
	}

	/** Relleno de {@code width} píxeles con piezas de 1..32; cada pieza avanza un píxel de más y se compensa. */
	private static void fill(StringBuilder sb, int base, int width) {
		for (int bit = MAX_FILL_BIT; bit >= 0; bit--) {
			int w = 1 << bit;
			while (width >= w) {
				sb.append((char) (base + bit)).append(NEG_1);
				width -= w;
			}
		}
	}

	private static String space(int delta) {
		StringBuilder sb = new StringBuilder();
		int base = delta < 0 ? NEG_SPACE : POS_SPACE;
		int left = Math.abs(delta);
		for (int bit = MAX_SPACE_BIT; bit >= 0; bit--) {
			int w = 1 << bit;
			while (left >= w) {
				sb.append((char) (base + bit));
				left -= w;
			}
		}
		return sb.toString();
	}
}
