package com.chaos.ranks.rank;

import org.jetbrains.annotations.Nullable;

import com.chaos.ranks.badge.BadgeMeta;

import net.minecraft.network.chat.Style;

/** Definición de un rango: etiqueta y formato del chat. Se guarda tal cual en ranks.json. */
public class RankDef {

	public static final int MAX_PRIORITY = 99;
	public static final int MAX_SEPARATOR = 6;
	public static final String DEFAULT_SEPARATOR = "»";

	public String id = "";
	public String label = "";
	@Nullable public String icon;
	public int textColor = 0xFFFFFF;
	public int iconColor = 0xFFFFFF;
	public int bgColor = 0xFF3B3B;
	@Nullable public Integer frameColor;
	public boolean bold;
	/** Más alto = antes en el Tab. */
	public int priority = 50;
	/** Lo tiene quien no tiene otro rango (como mucho un rango lo lleva). */
	public boolean defaultRank;
	/** Lo tienen los OP que no tienen otro rango asignado a mano (como mucho un rango lo lleva). */
	public boolean opRank;

	// Chat: "ETIQUETA Nombre » mensaje"
	public int nameRgb = 0xFFFFFF;
	public boolean nameBold;
	public String separator = DEFAULT_SEPARATOR;
	public int separatorColor = 0xAAAAAA;
	public int messageColor = 0xFFFFFF;

	public RankDef copy() {
		RankDef c = new RankDef();
		c.id = id;
		c.label = label;
		c.icon = icon;
		c.textColor = textColor;
		c.iconColor = iconColor;
		c.bgColor = bgColor;
		c.frameColor = frameColor;
		c.bold = bold;
		c.priority = priority;
		c.defaultRank = defaultRank;
		c.opRank = opRank;
		c.nameRgb = nameRgb;
		c.nameBold = nameBold;
		c.separator = separator;
		c.separatorColor = separatorColor;
		c.messageColor = messageColor;
		return c;
	}

	/** Estilo del nombre del jugador (chat, Tab y nametag). */
	public Style nameStyle() {
		return Style.EMPTY.withColor(nameRgb).withBold(nameBold);
	}

	/** Deja los valores dentro de lo permitido (lo que llega del cliente no es de fiar). */
	public void sanitize() {
		label = BadgeMeta.sanitize(label == null ? "" : label);
		if (BadgeMeta.icon(icon) == null) {
			icon = null;
		}
		textColor &= 0xFFFFFF;
		iconColor &= 0xFFFFFF;
		bgColor &= 0xFFFFFF;
		if (frameColor != null) {
			frameColor &= 0xFFFFFF;
		}
		priority = Math.max(0, Math.min(MAX_PRIORITY, priority));
		nameRgb &= 0xFFFFFF;
		separator = separator == null ? "" : separator.replace("§", "").strip();
		if (separator.length() > MAX_SEPARATOR) {
			separator = separator.substring(0, MAX_SEPARATOR);
		}
		separatorColor &= 0xFFFFFF;
		messageColor &= 0xFFFFFF;
	}
}
