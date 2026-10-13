package com.chaos.ranks.neoforge;
import com.chaos.ranks.platform.Platform;
import net.minecraftforge.network.PacketDistributor;
import com.chaos.ranks.net.RankPayloads.Message;
import net.minecraft.server.level.ServerPlayer;
public final class NeoforgePlatform implements Platform {
 public String name() { return "neoforge"; }
 public boolean canSend(ServerPlayer player) { return NeoforgeNetwork.CHANNEL.isRemotePresent(player.connection.connection); }
 public void sendToPlayer(ServerPlayer player, Message payload) { NeoforgeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload); }
 public void sendToServer(Message payload) { NeoforgeNetwork.CHANNEL.sendToServer(payload); }
}
