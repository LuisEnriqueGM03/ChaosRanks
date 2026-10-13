package com.chaos.ranks.fabric;
import com.chaos.ranks.*;
import com.chaos.ranks.command.RankCommand;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.server.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
public final class FabricRanks implements ModInitializer {
 public void onInitialize() {
  PayloadTypeRegistry.playS2C().register(RankPayloads.OpenManager.TYPE, RankPayloads.OpenManager.CODEC);
  PayloadTypeRegistry.playC2S().register(RankPayloads.Save.TYPE, RankPayloads.Save.CODEC);
  PayloadTypeRegistry.playC2S().register(RankPayloads.Delete.TYPE, RankPayloads.Delete.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Save.TYPE, (p,c) -> c.server().execute(() -> RankServer.save(c.player(),p)));
  ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Delete.TYPE, (p,c) -> c.server().execute(() -> RankServer.delete(c.player(),p)));
  CommandRegistrationCallback.EVENT.register((d,r,e) -> RankCommand.register(d));
  ServerLifecycleEvents.SERVER_STARTED.register(ChaosRanks::started);
  ServerPlayConnectionEvents.JOIN.register((h,s,server) -> ChaosRanks.joined(h.player));
  ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, RankChat::decorate);
 }
}
