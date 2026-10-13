package com.chaos.ranks.forge;
import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.command.RankCommand;
import com.chaos.ranks.server.RankChat;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraft.server.level.ServerPlayer;
@Mod(ChaosRanks.MOD_ID)
public final class ForgeRanks {
 public ForgeRanks() {
  ForgeNetwork.register();
  MinecraftForge.EVENT_BUS.addListener(this::commands);
  MinecraftForge.EVENT_BUS.addListener(this::started);
  MinecraftForge.EVENT_BUS.addListener(this::joined);
  MinecraftForge.EVENT_BUS.addListener(this::chat);
  if (FMLEnvironment.dist == Dist.CLIENT) ForgeClient.register();
 }
 private void commands(RegisterCommandsEvent event) { RankCommand.register(event.getDispatcher()); }
 private void started(ServerStartedEvent event) { ChaosRanks.started(event.getServer()); }
 private void joined(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer player) ChaosRanks.joined(player); }
 private void chat(ServerChatEvent event) { event.setMessage(RankChat.decorate(event.getPlayer(), event.getMessage())); }
}
