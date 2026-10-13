package com.chaos.ranks.neoforge;
import com.chaos.ranks.platform.Platform;
import com.chaos.ranks.net.RankPayloads;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
public final class NeoforgePlatform implements Platform {
 public String name() { return "neoforge"; }
 public boolean canSend(ServerPlayer player) { return NetworkRegistry.hasChannel(player.connection, RankPayloads.OpenManager.TYPE.id()); }
 public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) { PacketDistributor.sendToPlayer(player, payload); }
 public void sendToServer(CustomPacketPayload payload) { net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload); }
}
