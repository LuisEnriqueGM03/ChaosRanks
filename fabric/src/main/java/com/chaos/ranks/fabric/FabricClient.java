package com.chaos.ranks.fabric;
import com.chaos.ranks.client.ChaosRanksClient;
import com.chaos.ranks.net.RankPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
public final class FabricClient implements ClientModInitializer {
 public void onInitializeClient() {
  ClientPlayNetworking.registerGlobalReceiver(RankPayloads.OpenManager.TYPE, (p,c) -> ChaosRanksClient.receive(p));
  ClientPlayConnectionEvents.DISCONNECT.register((h,c) -> ChaosRanksClient.disconnected());
  ClientTickEvents.END_CLIENT_TICK.register(c -> ChaosRanksClient.tick());
 }
}
