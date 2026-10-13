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
  ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Save.TYPE, (server,player,handler,buf,sender) -> {
   var packet = RankPayloads.Save.CODEC.decode(buf); server.execute(() -> RankServer.save(player,packet));
  });
  ServerPlayNetworking.registerGlobalReceiver(RankPayloads.Delete.TYPE, (server,player,handler,buf,sender) -> {
   var packet = RankPayloads.Delete.CODEC.decode(buf); server.execute(() -> RankServer.delete(player,packet));
  });
  CommandRegistrationCallback.EVENT.register((d,r,e) -> RankCommand.register(d));
  ServerLifecycleEvents.SERVER_STARTED.register(ChaosRanks::started);
  ServerPlayConnectionEvents.JOIN.register((h,s,server) -> ChaosRanks.joined(h.player));
  ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, (sender,message) -> java.util.concurrent.CompletableFuture.completedFuture(RankChat.decorate(sender,message)));
 }
}
