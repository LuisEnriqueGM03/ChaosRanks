package com.chaos.ranks.neoforge;
import com.chaos.ranks.client.ChaosRanksClient;
import com.chaos.ranks.net.RankPayloads;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
public final class NeoforgeClient {
 private NeoforgeClient() {}
 public static void register() {
  MinecraftForge.EVENT_BUS.addListener(NeoforgeClient::tick);
  MinecraftForge.EVENT_BUS.addListener(NeoforgeClient::logout);
 }
 public static void receive(RankPayloads.OpenManager payload) { ChaosRanksClient.receive(payload); }
 private static void tick(TickEvent.ClientTickEvent event) { if (event.phase == TickEvent.Phase.END) ChaosRanksClient.tick(); }
 private static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ChaosRanksClient.disconnected(); }
}
