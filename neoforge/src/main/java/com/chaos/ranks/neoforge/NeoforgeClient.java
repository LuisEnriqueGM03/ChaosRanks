package com.chaos.ranks.neoforge;
import com.chaos.ranks.client.ChaosRanksClient;
import com.chaos.ranks.net.RankPayloads;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
public final class NeoforgeClient {
 private NeoforgeClient() {}
 public static void register() {
  NeoForge.EVENT_BUS.addListener(NeoforgeClient::tick);
  NeoForge.EVENT_BUS.addListener(NeoforgeClient::logout);
 }
 public static void receive(RankPayloads.OpenManager payload) { ChaosRanksClient.receive(payload); }
 private static void tick(ClientTickEvent.Post event) { ChaosRanksClient.tick(); }
 private static void logout(ClientPlayerNetworkEvent.LoggingOut event) { ChaosRanksClient.disconnected(); }
}
