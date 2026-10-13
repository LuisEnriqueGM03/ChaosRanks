package com.chaos.ranks.client;

import java.util.UUID;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.badge.BadgeRenderer;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;
import com.chaos.ranks.server.RankServer;


import com.chaos.ranks.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Autoprueba visual, solo en desarrollo (./gradlew runSelftest).
 * Crea rangos de ejemplo y hace capturas del chat, los nametags (de lejos y de cerca) y el menú.
 * Las capturas quedan en run/screenshots/selftest_*.png.
 */
public final class SelfTest {

	private static int ticks = -1;
 private static String waitingScreen = "";
 private static boolean openingWorld;
	private static UUID playerId;
	private static double standX;
	private static double standY;
	private static double standZ;

	private SelfTest() {}

	public static boolean enabled() {
		return System.getProperty("chaosranks.selftest") != null;
	}


	public static void tick(Minecraft mc) {
  if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
  // Open the isolated test world after loader initialization, through the normal title-screen flow.
  if (!openingWorld && mc.getSingleplayerServer() == null && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
   openingWorld = true;
   if (java.nio.file.Files.exists(mc.gameDirectory.toPath().resolve("saves/selftest/level.dat"))) {
    mc.createWorldOpenFlows().openWorld("selftest", () -> mc.setScreen(new net.minecraft.client.gui.screens.TitleScreen()));
   } else {
    mc.createWorldOpenFlows().createFreshLevel("selftest",
     new net.minecraft.world.level.LevelSettings("ChaosRanks selftest", net.minecraft.world.level.GameType.CREATIVE, new net.minecraft.world.level.LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false), true, net.minecraft.world.level.WorldDataConfiguration.DEFAULT),
     new net.minecraft.world.level.levelgen.WorldOptions(83L,false,false),
     registries -> registries.lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET).getOrThrow(net.minecraft.world.level.levelgen.presets.WorldPresets.FLAT).value().createWorldDimensions(), new net.minecraft.client.gui.screens.TitleScreen());
   }
  }
  if (openingWorld && mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen) {
   String proceed = Component.translatable("selectWorld.backupQuestion.skipButton").getString();
   screen.children().stream().filter(child -> child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals(proceed))
    .findFirst().ifPresent(child -> ((net.minecraft.client.gui.components.Button) child).onPress(new net.minecraft.client.input.MouseButtonInfo(0,0)));
  }
  if (mc.player == null && mc.screen != null) {
   String screen = mc.screen.getClass().getName();
   if (!screen.equals(waitingScreen)) { waitingScreen = screen; ChaosRanks.LOGGER.info("[selftest] waiting screen={}", screen); }
  }
		if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
			return;
		}
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		playerId = mc.player.getUUID();
		ticks++;

		switch (ticks) {
			case 30 -> mc.player.connection.sendChat("recien entrado, sin tocar nada");
			case 45 -> shot(mc, "join");
			case 48 -> server.execute(() -> {
				// Con un marcador en la lista, el Tab se ve aunque se juegue solo.
				var board = server.getScoreboard();
				var objective = board.getObjective("selftest_tab");
				if (objective == null) {
					objective = board.addObjective("selftest_tab", ObjectiveCriteria.DUMMY, Component.literal("Tab"),
							ObjectiveCriteria.RenderType.INTEGER, true, null);
				}
				board.setDisplayObjective(DisplaySlot.LIST, objective);
				server.getPlayerList().op(new net.minecraft.server.players.NameAndId(player(server).getGameProfile()));
			});
			case 52 -> mc.options.keyPlayerList.setDown(true);
			case 58 -> shot(mc, "tab");
			case 59 -> {
				mc.options.keyPlayerList.setDown(false);
				server.execute(() -> server.getScoreboard().setDisplayObjective(DisplaySlot.LIST, null));
			}
			case 60 -> server.execute(() -> setup(server));
			case 80 -> mc.player.connection.sendChat("soy OP: rango de OP automatico");
			case 84 -> server.execute(() -> server.getPlayerList().deop(new net.minecraft.server.players.NameAndId(player(server).getGameProfile())));
			case 88 -> mc.player.connection.sendChat("sin OP: vuelvo al rango por defecto");
			case 92 -> server.execute(() -> {
				server.getPlayerList().op(new net.minecraft.server.players.NameAndId(player(server).getGameProfile()));
				RankStore.assign(player(server).getUUID(), "owner");
				RankTeams.sync(server);
			});
			case 96 -> {
				// Guardado por la red, como el botón "Guardar": se renombra OWNER a JEFE y cambia el chat.
				RankDef d = RankStore.get("owner").copy();
				d.label = "JEFE";
				d.separator = "->";
				d.separatorColor = 0xFFC300;
				d.messageColor = 0xFFE14D;
				d.nameRgb = 0xFF6FB5;
				d.nameBold = true;
				Services.PLATFORM.sendToServer(new RankPayloads.Save(d, "owner", RankStore.revision()));
			}
			case 100 -> mc.setScreen(null); // el servidor responde al guardado abriendo el menú
			case 103 -> {
				mc.player.connection.sendChat("renombrado: ahora soy JEFE");
				ChaosRanks.LOGGER.info("[selftest] owner={} jefe={}", RankStore.get("owner") != null,
						RankStore.get("jefe") != null);
			}
			case 110 -> shot(mc, "chat");
   case 111 -> {
    try {
     var method = net.minecraft.client.gui.components.ChatComponent.class.getDeclaredMethod("getLineHeight");
     method.setAccessible(true);
     int height = (Integer) method.invoke(mc.gui.getChat());
     int expected = (int) (ChatSpacing.ROW_HEIGHT * (1 + mc.options.chatLineSpacing().get()));
     if (height != expected) throw new AssertionError("Chat row height mismatch: " + height);
     ChaosRanks.LOGGER.info("[selftest] chat spacing PASS: row={} badge={} gap={}", height, ChatSpacing.BADGE_HEIGHT, ChatSpacing.GAP);
    } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
   }
			case 115 -> server.execute(() -> RankServer.open(player(server), "jefe"));
   case 117 -> {
    if (Boolean.getBoolean("chaosranks.selftest.small")) { mc.options.guiScale().set(3); mc.resizeGui(); }
   }
   case 125 -> {
    if (Boolean.getBoolean("chaosranks.selftest.small") && mc.screen instanceof RankManagerScreen screen) {
     double scale = MenuGeometry.scale(screen.width, screen.height);
     String off = com.chaos.ranks.Lang.tr("button.bold", com.chaos.ranks.Lang.yesNo(false)).getString();
     var button = screen.children().stream().filter(child -> child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals(off)).findFirst().orElseThrow();
     var widget = (net.minecraft.client.gui.components.Button) button;
     if (!screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent((widget.getX() + widget.getWidth() / 2.0) * scale, (widget.getY() + widget.getHeight() / 2.0) * scale, new net.minecraft.client.input.MouseButtonInfo(0,0)), false)) throw new AssertionError("Scaled click was not handled");
     String on = com.chaos.ranks.Lang.tr("button.bold", com.chaos.ranks.Lang.yesNo(true)).getString();
     if (screen.children().stream().noneMatch(child -> child instanceof net.minecraft.client.gui.components.Button b && b.getMessage().getString().equals(on))) throw new AssertionError("Scaled click did not toggle bold");
     ChaosRanks.LOGGER.info("[selftest] scaled input PASS at {}x{}, scale={}", screen.width, screen.height, scale);
    }
   }
			case 130 -> shot(mc, "menu_label");
			case 132 -> tab(mc, 1);
			case 140 -> shot(mc, "menu_icon");
			case 142 -> tab(mc, 2);
			case 150 -> shot(mc, "menu_colors");
			case 152 -> tab(mc, 3);
			case 160 -> shot(mc, "menu_chat");
			case 165 -> mc.setScreen(null);
			default -> {}
		}

		// Nametags: de lejos y luego muy de cerca desde varios ángulos (para ver si parpadea).
		if (ticks >= 170 && ticks < 300) {
			int step = (ticks - 170) / 20;
			double dist = step == 0 ? 6 : 1.6;
			double angle = Math.toRadians((step - 1) * 20 - 40);
			double px = standX + Math.sin(angle) * dist;
			double pz = standZ + Math.cos(angle) * dist;
			double eye = mc.player.getEyeHeight();
			double ty = standY + 2.2;
			if ((ticks - 170) % 20 == 0) {
				server.execute(() -> player(server).teleportTo(px, ty - eye + 0.1, pz));
			}
			double dx = standX - mc.player.getX();
			double dz = standZ - mc.player.getZ();
			double dy = ty - mc.player.getEyeY();
			mc.player.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
			mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
			if ((ticks - 170) % 20 == 18) {
				shot(mc, "nametag_" + step);
			}
		}
		if (ticks == 305) {
			mc.stop();
		}
	}

	private static void tab(Minecraft mc, int index) {
		if (mc.screen instanceof RankManagerScreen screen) {
			screen.showTab(index);
		}
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayer(playerId);
	}

	private static void setup(MinecraftServer server) {
		ServerPlayer p = player(server);
		ServerLevel level = p.level();
		server.getPlayerList().op(new net.minecraft.server.players.NameAndId(p.getGameProfile()));
		level.getGameRules().set(GameRules.ADVANCE_TIME, false, server);


		rank("owner", "OWNER", "crown", 0xFF3B3B, 0xFFFFFF, 0x6B0F14, 99);
		rank("admin", "ADMIN", "shield", 0xB3131B, 0xFFFFFF, null, 90);
		rank("staff", "STAFF", "hammer", 0xFF8A1F, 0xFFFFFF, 0x7A3A00, 80);
		rank("moderator", "MODERATOR", "eye", 0x9B36E8, 0xFFFFFF, 0x3B2A5C, 70);
		rank("legendary", "LEGENDARY", "dragon", 0xFFC300, 0xFFFFFF, 0x8A5A00, 50);
		rank("diamond", "DIAMOND", "diamond", 0x3FD8EA, 0x0E3A44, 0x1599B0, 45);
		rank("vip", "VIP+", "plus", 0x2F5BFF, 0xFFFFFF, 0x1E1E1E, 40);
		rank("youtube", "YOUTUBE", "youtube", 0xFF0000, 0xFFFFFF, null, 30);
		rank("twitch", "TWITCH", "twitch", 0x9146FF, 0xFFFFFF, null, 30);
		rank("member", "MEMBER", "person", 0x7A7A7A, 0xFFFFFF, null, 1);
		RankStore.assign(p.getUUID(), null);
		RankTeams.sync(server);

		for (RankDef d : RankStore.ranks()) {
			p.sendSystemMessage(Component.empty().append(BadgeRenderer.build(d)).append(" Jugador" + d.priority));
		}

		standX = p.getX();
		standY = p.getY();
		standZ = p.getZ() - 5;
		ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
		stand.setPos(standX, standY, standZ);
		stand.setCustomName(Component.literal("Steve"));
		stand.setCustomNameVisible(true);
		stand.setNoGravity(true);
		level.addFreshEntity(stand);
		// En el mismo equipo que el jugador: el nametag se pinta igual que el de un jugador con ese rango.
		server.getScoreboard().addPlayerToTeam(stand.getStringUUID(), server.getScoreboard().getPlayersTeam(p.getScoreboardName()));
		ChaosRanks.LOGGER.info("[selftest] rangos creados");
	}

	private static void rank(String id, String label, String icon, int bg, int text, Integer frame, int priority) {
		RankDef d = new RankDef();
		d.id = id;
		d.label = label;
		d.icon = icon;
		d.bgColor = bg;
		d.textColor = text;
		d.iconColor = text;
		d.frameColor = frame;
		d.priority = priority;
		RankStore.put(d);
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, "selftest_" + (Boolean.getBoolean("chaosranks.selftest.small") ? "small_" : "") + name + ".png", mc.getMainRenderTarget(), 1,
				msg -> ChaosRanks.LOGGER.info("[selftest] {}", msg.getString()));
	}
}
