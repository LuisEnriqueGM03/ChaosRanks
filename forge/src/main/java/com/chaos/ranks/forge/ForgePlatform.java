package com.chaos.ranks.forge;
import com.chaos.ranks.platform.Platform;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
public final class ForgePlatform implements Platform {
 public String name() { return "forge"; }
 public boolean canSend(ServerPlayer player) { return ForgeNetwork.CHANNEL.isRemotePresent(player.connection.getConnection()); }
 public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) { ForgeNetwork.CHANNEL.send(payload, PacketDistributor.PLAYER.with(player)); }
 public void sendToServer(CustomPacketPayload payload) { ForgeNetwork.CHANNEL.send(payload, PacketDistributor.SERVER.noArg()); }
}
