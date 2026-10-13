package com.chaos.ranks.forge;
import com.chaos.ranks.platform.Platform;
import net.minecraftforge.network.PacketDistributor;
import com.chaos.ranks.net.RankPayloads.Message;
import net.minecraft.server.level.ServerPlayer;
public final class ForgePlatform implements Platform {
 public String name() { return "forge"; }
 public boolean canSend(ServerPlayer player) { return ForgeNetwork.CHANNEL.isRemotePresent(player.connection.connection); }
 public void sendToPlayer(ServerPlayer player, Message payload) { ForgeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload); }
 public void sendToServer(Message payload) { ForgeNetwork.CHANNEL.sendToServer(payload); }
}
