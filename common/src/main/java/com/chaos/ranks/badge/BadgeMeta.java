package com.chaos.ranks.badge;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Anchos y codepoints de la fuente de etiquetas (los genera tools/gen_font.py en badge_meta.json).
 * El servidor los necesita para calcular el tamaño exacto de cada etiqueta sin tener la fuente cargada.
 */
public final class BadgeMeta {

	public static final int MAX_LABEL = 16;

	/** Letra: ancho normal y en negrita, y los glifos de la letra en negrita y de los fondos recortados. */
	public record Letter(char glyph, int width, char knockout, char boldGlyph, int boldWidth, char boldKnockout) {}

	public record Icon(String id, char glyph, char knockout, int width) {}

	private static final Map<Character, Letter> LABEL = new HashMap<>();
	private static final Map<String, Icon> ICONS = new LinkedHashMap<>();
	private static int labelSpace = 3;

	static {
		try (InputStream in = BadgeMeta.class.getResourceAsStream("/assets/chaosranks/badge_meta.json")) {
			JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			labelSpace = root.get("labelSpace").getAsInt();
			for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("label").entrySet()) {
				JsonObject o = e.getValue().getAsJsonObject();
				char c = e.getKey().charAt(0);
				LABEL.put(c, new Letter(c, o.get("w").getAsInt(), (char) o.get("ko").getAsInt(),
						(char) o.get("b").getAsInt(), o.get("bw").getAsInt(), (char) o.get("bko").getAsInt()));
			}
			for (JsonElement el : root.getAsJsonArray("icons")) {
				JsonObject o = el.getAsJsonObject();
				Icon icon = new Icon(o.get("id").getAsString(), (char) o.get("char").getAsInt(),
						(char) o.get("ko").getAsInt(), o.get("width").getAsInt());
				ICONS.put(icon.id(), icon);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Falta assets/chaosranks/badge_meta.json", e);
		}
	}

	private BadgeMeta() {}

	public static int labelSpace() {
		return labelSpace;
	}

	@Nullable
	public static Letter letter(char c) {
		return LABEL.get(c);
	}

	@Nullable
	public static Icon icon(@Nullable String id) {
		return id != null ? ICONS.get(id) : null;
	}

	public static List<Icon> icons() {
		return Collections.unmodifiableList(new ArrayList<>(ICONS.values()));
	}

	/** Mayúsculas, sin tildes, solo caracteres que tiene la fuente y como mucho {@link #MAX_LABEL}. */
	public static String sanitize(String text) {
		String plain = Normalizer.normalize(text.toUpperCase(java.util.Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		StringBuilder sb = new StringBuilder();
		for (char c : plain.toCharArray()) {
			if (sb.length() >= MAX_LABEL) {
				break;
			}
			if (c == ' ' || LABEL.containsKey(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
