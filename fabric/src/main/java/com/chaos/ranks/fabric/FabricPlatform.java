package com.chaos.ranks.fabric;
import com.chaos.ranks.platform.Platform;
import com.chaos.ranks.net.RankPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
public final class FabricPlatform implements Platform {
 public String name() { return "fabric"; }
 public boolean canSend(ServerPlayer player) { return ServerPlayNetworking.canSend(player, RankPayloads.OpenManager.TYPE); }
 public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) { ServerPlayNetworking.send(player, payload); }
 public void sendToServer(CustomPacketPayload payload) { ClientPlayNetworking.send(payload); }
}
