package com.chaos.ranks.platform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
public interface Platform {
 String name();
 boolean canSend(ServerPlayer player);
 void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
 void sendToServer(CustomPacketPayload payload);
}
