package com.chaos.ranks.client;

import com.chaos.ranks.ChaosRanks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.chaos.ranks.net.RankPayloads;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ChaosRanksClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		RankPayloads.Assembler assembler = new RankPayloads.Assembler();
  ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> assembler.clear());
  ClientPlayNetworking.registerGlobalReceiver(RankPayloads.OpenManager.TYPE, (payload, context) ->
   context.client().execute(() -> {
    try {
     var ranks = assembler.accept(payload);
     if (ranks == null) return;
     if (context.client().screen instanceof RankManagerScreen screen) screen.update(ranks, payload.selectId(), payload.revision());
     else context.client().setScreen(new RankManagerScreen(ranks, payload.selectId(), payload.revision()));
    } catch (Exception e) { assembler.clear(); ChaosRanks.LOGGER.warn("Invalid rank manager response", e); }
   }));

		if (SelfTest.enabled()) {
			SelfTest.register();
		}
	}
}
