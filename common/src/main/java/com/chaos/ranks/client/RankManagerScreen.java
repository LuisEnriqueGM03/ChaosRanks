package com.chaos.ranks.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.Lang;
import com.chaos.ranks.badge.BadgeMeta;
import com.chaos.ranks.badge.BadgeRenderer;
import com.chaos.ranks.badge.Palette;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.server.RankChat;

import com.chaos.ranks.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

/**
 * Menú de rangos (/rank): lista a la izquierda, vista previa arriba y el editor por pestañas.
 * Texturas propias (tools/gen_gui.py). Solo guarda al pulsar "Guardar"; el servidor valida, guarda y devuelve
 * la lista actualizada.
 */
public class RankManagerScreen extends Screen {

	private enum Tab {
		LABEL, ICON, COLORS, CHAT;

		Component title() {
			return Lang.tr("tab." + name().toLowerCase(java.util.Locale.ROOT));
		}
	}

	private enum Target {
		BACKGROUND, TEXT, ICON, FRAME, NAME, MESSAGE, SEPARATOR;

		Component title() {
			return Lang.tr("target." + name().toLowerCase(java.util.Locale.ROOT));
		}
	}

	private static final Identifier PANEL = ChaosRanks.id("panel");
	private static final Identifier INSET = ChaosRanks.id("inset");
	private static final Identifier CELL = ChaosRanks.id("cell");

	private static final int W = 420;
	private static final int H = 236;
	private static final int PAD = 9;
	private static final int LIST_W = 118;
	private static final int TOP = 26;
	private static final int ROW_H = 14;
	private static final int RX = PAD + LIST_W + 6;
	private static final int RW = W - RX - PAD;
	private static final int PREVIEW_BOTTOM = 74;
	private static final int TABS_Y = 78;
	private static final int CONTENT_Y = 99;
	private static final int BOTTOM_Y = H - 30;
	private static final int LIST_BOTTOM = BOTTOM_Y - 5;

	private static final int ICON_CELL = 18;
	private static final int ICON_COLS = RW / ICON_CELL;
	private static final int ICON_TOP = CONTENT_Y + 21;
	private static final int ICON_ROWS = 4;

	private static final int SWATCH = 16;
	private static final int SWATCH_STEP = 20;
	private static final int SWATCH_COLS = 14;

	private static final int GOLD = 0xFFE8B23A;
	private static final int GOLD_LIGHT = 0xFFFFE08A;
	private static final int INK = 0xFF0C0704;
	private static final int ROW_SELECTED = 0xFF5A3A14;
	private static final int ROW_HOVER = 0xFF3A2410;
	private static final int LABEL = 0xFFE8C77A;
	private static final int HINT = 0xFF9A8466;
	private static final int FIELD_TEXT = 0xFFFFEBC0;

	private List<RankDef> ranks;
 private long revision;
 private double uiScale = 1;
	@Nullable private String originalId;
	private RankDef draft;
	private Tab tab = Tab.LABEL;
	private Target colorTarget = Target.BACKGROUND;
	private Target chatTarget = Target.NAME;
	private int listScroll;
	private int iconScroll;
	private boolean confirmDelete;

	@Nullable private EditBox hexBox;
	/** Recuadros hundidos de los campos de texto de la pestaña actual (x, y, ancho, alto). */
	private final List<int[]> fields = new ArrayList<>();

	public RankManagerScreen(List<RankDef> ranks, String selectId, long revision) {
		super(Minecraft.getInstance(), Minecraft.getInstance().font, Lang.tr("screen.title"));
  this.revision = revision;
		this.ranks = ranks;
		select(selectId);
	}

	/** Respuesta del servidor tras guardar o borrar: lista nueva, mismo menú. */
	public void update(List<RankDef> ranks, String selectId, long revision) {
		this.ranks = ranks;
  this.revision = revision;
  listScroll = Math.min(listScroll, maxListScroll());
		select(selectId.isEmpty() && originalId != null ? originalId : selectId);
		rebuildWidgets();
	}

	private void select(String id) {
		confirmDelete = false;
		for (RankDef def : ranks) {
			if (def.id.equals(id)) {
				draft = def.copy();
				originalId = def.id;
				return;
			}
		}
		if (!ranks.isEmpty() && id.isEmpty()) {
			draft = ranks.get(0).copy();
			originalId = draft.id;
			return;
		}
		newDraft();
	}

	private void newDraft() {
		RankDef def = new RankDef();
		def.label = Lang.tr("screen.new_label").getString();
		def.icon = "star";
		def.frameColor = 0x1E1E1E;
		draft = def;
		originalId = null;
		confirmDelete = false;
	}

	private int left() {
		return (int) ((width / uiScale - W) / 2);
	}

	private int top() {
		return (int) ((height / uiScale - H) / 2);
	}

	// ---------------------------------------------------------------------------------------------
	// Widgets

	private GoldButton button(int x, int y, int w, int h, Component text, Runnable action) {
		return addRenderableWidget(new GoldButton(x, y, w, h, text, b -> action.run()));
	}

	/** Campo de texto sin el borde de vanilla: el recuadro lo pinta el menú con su textura. */
	private EditBox field(int x, int y, int w, int maxLength, String value) {
		fields.add(new int[] {x, y, w, 18});
		EditBox box = new EditBox(font, x + 5, y + 1, w - 10, 16, Component.empty());
		box.setBordered(false);
		box.setTextColor(FIELD_TEXT);
		box.setMaxLength(maxLength);
		box.setValue(value);
		addRenderableWidget(box);
		return box;
	}

	@Override
	protected void init() {
  uiScale = MenuGeometry.scale(width, height);
		int x = left();
		int y = top();
		hexBox = null;
		fields.clear();

		button(x + PAD, y + BOTTOM_Y, LIST_W, 20, Lang.tr("button.new"), () -> {
			newDraft();
			rebuildWidgets();
		});

		for (Tab t : Tab.values()) {
			button(x + RX + t.ordinal() * 70, y + TABS_Y, 68, 16, t.title(), () -> {
				tab = t;
				rebuildWidgets();
			}).selected(tab == t);
		}

		button(x + RX, y + BOTTOM_Y, 90, 20, Lang.tr("button.save"), () ->
				Services.PLATFORM.sendToServer(new RankPayloads.Save(draft, originalId == null ? "" : originalId, revision)));
		GoldButton delete = button(x + RX + 95, y + BOTTOM_Y, 90, 20, Lang.tr(confirmDelete ? "button.confirm_delete" : "button.delete"), () -> {
			if (confirmDelete && originalId != null) {
				Services.PLATFORM.sendToServer(new RankPayloads.Delete(originalId, revision));
			} else {
				confirmDelete = true;
				rebuildWidgets();
			}
		});
		delete.active = originalId != null;
		button(x + RX + 190, y + BOTTOM_Y, 90, 20, Lang.tr("button.close"), this::onClose);

		switch (tab) {
			case LABEL -> initLabelTab(x + RX, y + CONTENT_Y);
			case ICON -> initIconTab(x + RX, y + CONTENT_Y);
			case COLORS -> initColorsTab(x + RX, y + CONTENT_Y);
			case CHAT -> initChatTab(x + RX, y + CONTENT_Y);
		}
	}

	private void initLabelTab(int x, int y) {
		EditBox label = field(x + 44, y, 170, BadgeMeta.MAX_LABEL, draft.label);
		label.setResponder(text -> {
			String clean = BadgeMeta.sanitize(text);
			if (!clean.equals(text)) {
				label.setValue(clean);
				return;
			}
			draft.label = clean;
		});
		setInitialFocus(label);

		button(x, y + 24, 88, 18, Lang.tr("button.bold", Lang.yesNo(draft.bold)), () -> {
			draft.bold = !draft.bold;
			rebuildWidgets();
		});
		button(x + 91, y + 24, 93, 18, Lang.tr("button.default", Lang.yesNo(draft.defaultRank)), () -> {
			draft.defaultRank = !draft.defaultRank;
			rebuildWidgets();
		});
		button(x + 187, y + 24, 92, 18, Lang.tr("button.op", Lang.yesNo(draft.opRank)), () -> {
			draft.opRank = !draft.opRank;
			rebuildWidgets();
		});

		int[] steps = {-10, -1, 1, 10};
		for (int i = 0; i < steps.length; i++) {
			int step = steps[i];
			button(x + 136 + i * 36, y + 48, 34, 18, Component.literal((step > 0 ? "+" : "") + step), () ->
					draft.priority = Math.max(0, Math.min(RankDef.MAX_PRIORITY, draft.priority + step)));
		}
	}

	private void initIconTab(int x, int y) {
		button(x, y, 70, 16, Lang.tr("button.no_icon"), () -> { draft.icon = null; rebuildWidgets(); }).selected(draft.icon == null);
	}

	private void initColorsTab(int x, int y) {
		Target[] targets = {Target.BACKGROUND, Target.TEXT, Target.ICON, Target.FRAME};
		for (int i = 0; i < targets.length; i++) {
			Target t = targets[i];
			button(x + i * 70, y, 68, 16, t.title(), () -> {
				colorTarget = t;
				rebuildWidgets();
			}).selected(colorTarget == t);
		}
		if (colorTarget == Target.FRAME) {
			button(x, y + 66, 80, 18, Lang.tr("button.no_frame"), () -> {
				draft.frameColor = null;
				rebuildWidgets();
			}).selected(draft.frameColor == null);
		}
		addHexBox(x + RW - 76, y + 66, colorTarget);
	}

	private void initChatTab(int x, int y) {
		EditBox sep = field(x + 62, y, 56, RankDef.MAX_SEPARATOR, draft.separator);
		sep.setResponder(text -> draft.separator = text.replace("§", ""));

		button(x + 128, y, 152, 18, Lang.tr("button.name_bold", Lang.yesNo(draft.nameBold)), () -> {
			draft.nameBold = !draft.nameBold;
			rebuildWidgets();
		});

		Target[] targets = {Target.NAME, Target.MESSAGE, Target.SEPARATOR};
		for (int i = 0; i < targets.length; i++) {
			Target t = targets[i];
			button(x + i * 94, y + 22, 92, 16, t.title(), () -> {
				chatTarget = t;
				rebuildWidgets();
			}).selected(chatTarget == t);
		}
		addHexBox(x + RW - 76, y + 86, chatTarget);
	}

	private void addHexBox(int x, int y, Target target) {
		Integer c = get(target);
		hexBox = field(x, y, 76, 7, c == null ? "" : String.format("#%06X", c));
		hexBox.setResponder(text -> {
			String hex = text.startsWith("#") ? text.substring(1) : text;
			if (hex.matches("[0-9a-fA-F]{6}")) {
				set(target, Integer.parseInt(hex, 16));
			}
		});
	}

	private void syncHex() {
		if (hexBox != null) {
			Integer c = get(tab == Tab.CHAT ? chatTarget : colorTarget);
			hexBox.setValue(c == null ? "" : String.format("#%06X", c));
		}
	}

	@Nullable
	private Integer get(Target t) {
		return switch (t) {
			case BACKGROUND -> draft.bgColor;
			case TEXT -> draft.textColor;
			case ICON -> draft.iconColor;
			case FRAME -> draft.frameColor;
			case NAME -> draft.nameRgb;
			case MESSAGE -> draft.messageColor;
			case SEPARATOR -> draft.separatorColor;
		};
	}

	private void set(Target t, int rgb) {
		switch (t) {
			case BACKGROUND -> draft.bgColor = rgb;
			case TEXT -> draft.textColor = rgb;
			case ICON -> draft.iconColor = rgb;
			case FRAME -> draft.frameColor = rgb;
			case NAME -> draft.nameRgb = rgb;
			case MESSAGE -> draft.messageColor = rgb;
			case SEPARATOR -> draft.separatorColor = rgb;
		}
	}

	private int swatchTop() {
		return tab == Tab.COLORS ? CONTENT_Y + 22 : CONTENT_Y + 44;
	}

	// ---------------------------------------------------------------------------------------------
	// Ratón

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
  double mouseX = event.x(), mouseY = event.y(); int button = event.button();
  mouseX /= uiScale; mouseY /= uiScale;
		if (super.mouseClicked(new MouseButtonEvent(mouseX,mouseY,event.buttonInfo()),doubleClick)) {
			return true;
		}
		int mx = (int) mouseX - left();
		int my = (int) mouseY - top();

		if (mx >= PAD && mx < PAD + LIST_W && my >= TOP + 2 && my < LIST_BOTTOM - 2) {
			int index = (my - TOP - 2 + listScroll) / ROW_H;
			if (index >= 0 && index < ranks.size()) {
				select(ranks.get(index).id);
				rebuildWidgets();
				click();
				return true;
			}
		}
		if (tab == Tab.ICON) {
			int index = iconAt(mx, my);
			if (index >= 0) {
				draft.icon = BadgeMeta.icons().get(index).id();
				rebuildWidgets();
				click();
				return true;
			}
		}
		if (tab == Tab.COLORS || tab == Tab.CHAT) {
			int index = swatchAt(mx, my, swatchTop());
			if (index >= 0) {
				set(tab == Tab.COLORS ? colorTarget : chatTarget, Palette.COLORS.get(index).rgb());
				if (tab == Tab.COLORS && colorTarget == Target.FRAME) {
					rebuildWidgets();
				} else {
					syncHex();
				}
				click();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
  mouseX /= uiScale; mouseY /= uiScale;
		int mx = (int) mouseX - left();
		if (mx >= PAD && mx < PAD + LIST_W && mouseY - top() >= TOP && mouseY - top() < LIST_BOTTOM) {
			int max = maxListScroll();
			listScroll = Math.max(0, Math.min(max, listScroll - (int) (scrollY * ROW_H)));
			return true;
		}
		if (tab == Tab.ICON) {
			iconScroll = Math.max(0, Math.min(maxIconScroll(), iconScroll - (int) Math.signum(scrollY)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

 private int maxListScroll() { return Math.max(0, ranks.size() * ROW_H - (LIST_BOTTOM - TOP - 4)); }
 @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / uiScale, y / uiScale); }
 @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(new MouseButtonEvent(event.x()/uiScale,event.y()/uiScale,event.buttonInfo())); }
 @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) { return super.mouseDragged(new MouseButtonEvent(event.x()/uiScale,event.y()/uiScale,event.buttonInfo()),dx/uiScale,dy/uiScale); }

	private int maxIconScroll() {
		int rows = (BadgeMeta.icons().size() + ICON_COLS - 1) / ICON_COLS;
		return Math.max(0, rows - ICON_ROWS);
	}

	private int iconAt(int mx, int my) {
		int col = (mx - RX) / ICON_CELL;
		int row = (my - ICON_TOP) / ICON_CELL;
		if (mx < RX || my < ICON_TOP || col >= ICON_COLS || row >= ICON_ROWS) {
			return -1;
		}
		int index = (row + iconScroll) * ICON_COLS + col;
		return index < BadgeMeta.icons().size() ? index : -1;
	}

	private int swatchAt(int mx, int my, int top) {
		int col = (mx - RX) / SWATCH_STEP;
		int row = (my - top) / SWATCH_STEP;
		if (mx < RX || my < top || col >= SWATCH_COLS || (mx - RX) % SWATCH_STEP >= SWATCH
				|| (my - top) % SWATCH_STEP >= SWATCH) {
			return -1;
		}
		int index = row * SWATCH_COLS + col;
		return index < Palette.COLORS.size() ? index : -1;
	}

	private void click() {
		if (minecraft != null) {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
	}

	// ---------------------------------------------------------------------------------------------
	// Dibujo

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {}

 private void renderPanel(GuiGraphicsExtractor g) {

		int x = left();
		int y = top();
		g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, PANEL, x, y, W, H);
		g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, INSET, x + PAD, y + TOP, LIST_W, LIST_BOTTOM - TOP);
		g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, INSET, x + RX, y + TOP, RW, PREVIEW_BOTTOM - TOP);
		// Línea dorada bajo el título.
		g.fill(x + PAD, y + 22, x + W - PAD, y + 23, 0xFF8A5A12);
		for (int[] f : fields) {
			g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, INSET, f[0], f[1], f[2], f[3]);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {

  g.pose().pushMatrix();
  g.pose().scale((float) uiScale, (float) uiScale);
  mouseX = (int) (mouseX / uiScale); mouseY = (int) (mouseY / uiScale);
		renderPanel(g);
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int x = left();
		int y = top();
		int mx = mouseX - x;
		int my = mouseY - y;

		renderTitle(g, x, y);
		renderList(g, x, y, mx, my);
		renderPreview(g, x, y);

		switch (tab) {
			case LABEL -> renderLabelTab(g, x + RX, y + CONTENT_Y);
			case ICON -> renderIconTab(g, x, y, mx, my, mouseX, mouseY);
			case COLORS -> {
				renderSwatches(g, x, y, get(colorTarget), mx, my, mouseX, mouseY);
				g.text(font, Lang.tr("screen.hex"), x + RX + RW - 100, y + CONTENT_Y + 71, LABEL, false);
			}
			case CHAT -> {
				g.text(font, Lang.tr("screen.separator"), x + RX, y + CONTENT_Y + 5, LABEL, false);
				g.text(font, Lang.tr("screen.hex"), x + RX + RW - 100, y + CONTENT_Y + 91, LABEL, false);
				renderSwatches(g, x, y, get(chatTarget), mx, my, mouseX, mouseY);
			}
		}
  g.pose().popMatrix();
	}

	private void renderTitle(GuiGraphicsExtractor g, int x, int y) {
		RankDef title = new RankDef();
		title.label = "CHAOS RANKS";
		title.icon = "crown";
		title.bgColor = 0xE8B23A;
		title.frameColor = 0x5A3A08;
		title.textColor = 0x2A1606;
		title.iconColor = 0x2A1606;
		g.text(font, BadgeRenderer.build(title), x + PAD + 1, y + 10, 0xFFFFFFFF, false);
		Component editing = originalId == null ? Lang.tr("screen.new_rank") : Lang.tr("screen.editing", originalId);
		g.text(font, editing, x + W - PAD - 2 - font.width(editing), y + 10, LABEL, false);
	}

 // Minecraft 1.21.4 applies the current pose to scissor bounds.
 private void scissor(GuiGraphicsExtractor g, int left, int top, int right, int bottom) {
  g.enableScissor(left, top, right, bottom);
 }

	private void renderList(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
		int top = y + TOP + 2;
		int bottom = y + LIST_BOTTOM - 2;
		scissor(g, x + PAD + 2, top, x + PAD + LIST_W - 2, bottom);
		for (int i = 0; i < ranks.size(); i++) {
			RankDef def = ranks.get(i);
			int ry = top + i * ROW_H - listScroll;
			boolean hovered = mx >= PAD && mx < PAD + LIST_W && my + y >= ry && my + y < ry + ROW_H
					&& my + y >= top && my + y < bottom;
			if (def.id.equals(originalId)) {
				g.fill(x + PAD + 2, ry, x + PAD + LIST_W - 2, ry + ROW_H, ROW_SELECTED);
				g.fill(x + PAD + 2, ry, x + PAD + 4, ry + ROW_H, GOLD);
			} else if (hovered) {
				g.fill(x + PAD + 2, ry, x + PAD + LIST_W - 2, ry + ROW_H, ROW_HOVER);
			}
			g.text(font, BadgeRenderer.build(def), x + PAD + 7, ry + 4, 0xFFFFFFFF, true);
		}
		g.disableScissor();
		if (ranks.isEmpty()) {
			g.centeredText(font, Lang.tr("screen.empty"), x + PAD + LIST_W / 2, top + 8, HINT);
		}
	}

	private void renderPreview(GuiGraphicsExtractor g, int x, int y) {
		MutableComponent badge = BadgeRenderer.build(draft);
		int bw = font.width(badge);
		g.pose().pushMatrix();
		float previewScale = Math.min(2f, (RW - 16f) / Math.max(1, bw));
  g.pose().translate(x + RX + (RW - bw * previewScale) / 2f, y + TOP + 10);
  g.pose().scale(previewScale, previewScale);
		g.text(font, badge, 0, 0, 0xFFFFFFFF, true);
		g.pose().popMatrix();

		String name = minecraft != null && minecraft.player != null ? minecraft.player.getScoreboardName() : "Steve";
		Component chat = Component.empty().append(BadgeRenderer.build(draft)).append(" ")
				.append(Component.literal(name).setStyle(draft.nameStyle()))
				.append(" ")
				.append(RankChat.format(draft.separator, draft.separatorColor, draft.messageColor,
						Lang.tr("screen.sample_message")));
  scissor(g, x + RX + 4, y + TOP + 2, x + RX + RW - 4, y + PREVIEW_BOTTOM - 2);
		g.text(font, chat, x + RX + 8, y + PREVIEW_BOTTOM - 14, 0xFFFFFFFF, true);
  g.disableScissor();
	}

	private void renderLabelTab(GuiGraphicsExtractor g, int x, int y) {
		g.text(font, Lang.tr("screen.text"), x, y + 5, LABEL, false);
		g.text(font, Lang.tr("screen.priority"), x, y + 53, LABEL, false);
		g.text(font, String.valueOf(draft.priority), x + 60, y + 53, 0xFFFFFFFF, true);
		g.text(font, Lang.tr("screen.hint.priority"), x, y + 71, HINT, false);
		g.text(font, Lang.tr("screen.hint.default"), x, y + 80, HINT, false);
		g.text(font, Lang.tr("screen.hint.op"), x, y + 89, HINT, false);
	}

	private void renderIconTab(GuiGraphicsExtractor g, int x, int y, int mx, int my, int mouseX, int mouseY) {
		List<BadgeMeta.Icon> icons = BadgeMeta.icons();
		BadgeMeta.Icon current = BadgeMeta.icon(draft.icon);
		g.text(font, current == null ? Lang.tr("button.no_icon") : Lang.tr("screen.icon", current.id()),
				x + RX + 78, y + CONTENT_Y + 4, LABEL, false);
		Component page = Component.literal((iconScroll + 1) + "/" + (maxIconScroll() + 1) + "  ").append(Lang.tr("screen.scroll"));
		g.text(font, page, x + RX + RW - font.width(page), y + CONTENT_Y + 4, HINT, false);

		int hovered = iconAt(mx, my);
		for (int r = 0; r < ICON_ROWS; r++) {
			for (int c = 0; c < ICON_COLS; c++) {
				int index = (r + iconScroll) * ICON_COLS + c;
				if (index >= icons.size()) {
					continue;
				}
				BadgeMeta.Icon icon = icons.get(index);
				int cx = x + RX + c * ICON_CELL;
				int cy = y + ICON_TOP + r * ICON_CELL;
				boolean selected = current != null && current.id().equals(icon.id());
				if (selected) {
					g.fill(cx - 1, cy - 1, cx + ICON_CELL - 1, cy + ICON_CELL - 1, GOLD_LIGHT);
					g.fill(cx, cy, cx + ICON_CELL - 2, cy + ICON_CELL - 2, 0xFF000000 | draft.bgColor);
				} else {
					g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, CELL, cx, cy, ICON_CELL - 2, ICON_CELL - 2);
					if (index == hovered) {
						g.fill(cx + 1, cy + 1, cx + ICON_CELL - 3, cy + ICON_CELL - 3, 0x40FFE08A);
					}
				}
				g.pose().pushMatrix();
				g.pose().translate(cx + 1, cy + 1);
				g.pose().scale(2, 2);
				g.text(font, BadgeRenderer.icon(icon, selected ? draft.iconColor : 0xFFEBC0), 0, 0, 0xFFFFFFFF, false);
				g.pose().popMatrix();
			}
		}
		if (hovered >= 0) {
			g.setTooltipForNextFrame(font, Component.literal(icons.get(hovered).id()), mouseX, mouseY);
		}
	}

	private void renderSwatches(GuiGraphicsExtractor g, int x, int y, @Nullable Integer current, int mx, int my,
			int mouseX, int mouseY) {
		int top = swatchTop();
		int hovered = swatchAt(mx, my, top);
		for (int i = 0; i < Palette.COLORS.size(); i++) {
			Palette.Color color = Palette.COLORS.get(i);
			int cx = x + RX + (i % SWATCH_COLS) * SWATCH_STEP;
			int cy = y + top + (i / SWATCH_COLS) * SWATCH_STEP;
			boolean selected = current != null && current == color.rgb();
			g.fill(cx - 1, cy - 1, cx + SWATCH + 1, cy + SWATCH + 1, selected ? GOLD_LIGHT : i == hovered ? GOLD : INK);
			if (selected) {
				g.fill(cx, cy, cx + SWATCH, cy + SWATCH, GOLD);
				g.fill(cx + 1, cy + 1, cx + SWATCH - 1, cy + SWATCH - 1, 0xFF000000 | color.rgb());
			} else {
				g.fill(cx, cy, cx + SWATCH, cy + SWATCH, 0xFF000000 | color.rgb());
				// Brillo y sombra de 1 px para que parezcan botones.
				g.fill(cx, cy, cx + SWATCH, cy + 1, 0x40FFFFFF);
				g.fill(cx, cy + SWATCH - 1, cx + SWATCH, cy + SWATCH, 0x50000000);
			}
		}
		if (hovered >= 0) {
			Palette.Color c = Palette.COLORS.get(hovered);
			g.setTooltipForNextFrame(font, c.name().copy().append(String.format("  #%06X", c.rgb())), mouseX, mouseY);
		}
	}

	/** Para la autoprueba: cambia de pestaña como si se pulsara su botón. */
	void showTab(int index) {
		tab = Tab.values()[index];
		rebuildWidgets();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
