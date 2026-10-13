package com.chaos.ranks.forge;
import com.chaos.ranks.client.ChaosRanksClient;
import com.chaos.ranks.net.RankPayloads;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
public final class ForgeClient {
 private ForgeClient() {}
 public static void register() {
  MinecraftForge.EVENT_BUS.addListener(ForgeClient::tick);
  MinecraftForge.EVENT_BUS.addListener(ForgeClient::logout);
 }
 public static void receive(RankPayloads.OpenManager payload) { ChaosRanksClient.receive(payload); }
 private static void tick(TickEvent.ClientTickEvent.Post event) { ChaosRanksClient.tick(); }
 private static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ChaosRanksClient.disconnected(); }
}
