package com.chaos.ranks.platform;
import com.chaos.ranks.net.RankPayloads.Message;
import net.minecraft.server.level.ServerPlayer;
public interface Platform {
 String name();
 boolean canSend(ServerPlayer player);
 void sendToPlayer(ServerPlayer player, Message payload);
 void sendToServer(Message payload);
}
