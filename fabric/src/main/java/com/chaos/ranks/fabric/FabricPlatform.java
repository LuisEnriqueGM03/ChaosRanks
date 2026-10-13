package com.chaos.ranks.fabric;
import com.chaos.ranks.platform.Platform;
import com.chaos.ranks.net.RankPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.chaos.ranks.net.RankPayloads.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.server.level.ServerPlayer;
public final class FabricPlatform implements Platform {
 public String name() { return "fabric"; }
 public boolean canSend(ServerPlayer player) { return ServerPlayNetworking.canSend(player, RankPayloads.OpenManager.TYPE); }
 public void sendToPlayer(ServerPlayer player, Message payload) { ServerPlayNetworking.send(player, payload.type(), encode(payload)); }
 public void sendToServer(Message payload) { ClientPlayNetworking.send(payload.type(), encode(payload)); }
 private static FriendlyByteBuf encode(Message payload) {
  var buffer = PacketByteBufs.create();
  if(payload instanceof RankPayloads.OpenManager p) RankPayloads.OpenManager.CODEC.encode(buffer,p);
  else if(payload instanceof RankPayloads.Save p) RankPayloads.Save.CODEC.encode(buffer,p);
  else if(payload instanceof RankPayloads.Delete p) RankPayloads.Delete.CODEC.encode(buffer,p);
  else throw new IllegalArgumentException("Unknown rank message");
  return buffer;
 }
}
