package com.chaos.ranks.neoforge;
import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.command.RankCommand;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.server.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.server.level.ServerPlayer;
@Mod(ChaosRanks.MOD_ID)
public final class NeoforgeRanks {
 public NeoforgeRanks(IEventBus bus) {
  bus.addListener(this::payloads);
  NeoForge.EVENT_BUS.addListener(this::commands);
  NeoForge.EVENT_BUS.addListener(this::started);
  NeoForge.EVENT_BUS.addListener(this::joined);
  NeoForge.EVENT_BUS.addListener(this::chat);
  if (FMLEnvironment.dist == Dist.CLIENT) NeoforgeClient.register();
 }
 private void payloads(RegisterPayloadHandlersEvent event) {
  var registrar = event.registrar("2").optional();
  registrar.playToClient(RankPayloads.OpenManager.TYPE, RankPayloads.OpenManager.CODEC, (p,c) -> NeoforgeClient.receive(p));
  registrar.playToServer(RankPayloads.Save.TYPE, RankPayloads.Save.CODEC, (p,c) -> { if (c.player() instanceof ServerPlayer player) RankServer.save(player,p); });
  registrar.playToServer(RankPayloads.Delete.TYPE, RankPayloads.Delete.CODEC, (p,c) -> { if (c.player() instanceof ServerPlayer player) RankServer.delete(player,p); });
 }
 private void commands(RegisterCommandsEvent event) { RankCommand.register(event.getDispatcher()); }
 private void started(ServerStartedEvent event) { ChaosRanks.started(event.getServer()); }
 private void joined(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer player) ChaosRanks.joined(player); }
 private void chat(ServerChatEvent event) { event.setMessage(RankChat.decorate(event.getPlayer(), event.getMessage())); }
}
