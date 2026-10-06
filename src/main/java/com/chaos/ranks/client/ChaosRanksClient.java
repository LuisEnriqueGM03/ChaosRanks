package com.chaos.ranks.client;

import com.chaos.ranks.net.RankPayloads;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ChaosRanksClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RankPayloads.OpenManager.TYPE, (payload, context) ->
				context.client().execute(() -> {
					if (context.client().screen instanceof RankManagerScreen screen) {
						screen.update(payload.ranks(), payload.selectId());
					} else {
						context.client().setScreen(new RankManagerScreen(payload.ranks(), payload.selectId()));
					}
				}));

		if (SelfTest.enabled()) {
			SelfTest.register();
		}
	}
}
