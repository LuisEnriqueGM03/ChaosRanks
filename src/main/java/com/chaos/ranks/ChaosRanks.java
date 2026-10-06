package com.chaos.ranks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chaos.ranks.command.RankCommand;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;
import com.chaos.ranks.server.RankChat;
import com.chaos.ranks.server.RankServer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;

public class ChaosRanks implements ModInitializer {

	public static final String MOD_ID = "chaosranks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(RankPayloads.OpenManager.TYPE, RankPayloads.OpenManager.CODEC);
		PayloadTypeRegistry.playC2S().register(RankPayloads.Save.TYPE, RankPayloads.Save.CODEC);
		PayloadTypeRegistry.playC2S().register(RankPayloads.Delete.TYPE, RankPayloads.Delete.CODEC);
		RankServer.register();
		RankChat.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				RankCommand.register(dispatcher));

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			RankStore.load(server);
			RankTeams.sync(server);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> RankTeams.sync(server, handler.player));
	}
}
