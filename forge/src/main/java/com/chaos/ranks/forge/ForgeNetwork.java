package com.chaos.ranks.forge;
import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.net.RankPayloads;
import com.chaos.ranks.server.RankServer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
public final class ForgeNetwork {
 public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(ChaosRanks.id("editor_v2")).networkProtocolVersion(() -> "2").clientAcceptedVersions(v -> v.equals("2") || v.equals(NetworkRegistry.ABSENT)).serverAcceptedVersions(v -> v.equals("2") || v.equals(NetworkRegistry.ABSENT)).simpleChannel();
 private ForgeNetwork() {}
 public static void register() {
  CHANNEL.messageBuilder(RankPayloads.OpenManager.class, 0, NetworkDirection.PLAY_TO_CLIENT)
   .encoder((p,b) -> RankPayloads.OpenManager.CODEC.encode(b,p)).decoder(RankPayloads.OpenManager.CODEC::decode)
   .consumerMainThread((p,c) -> ForgeClient.receive(p)).add();
  CHANNEL.messageBuilder(RankPayloads.Save.class, 1, NetworkDirection.PLAY_TO_SERVER)
   .encoder((p,b) -> RankPayloads.Save.CODEC.encode(b,p)).decoder(RankPayloads.Save.CODEC::decode)
   .consumerMainThread((p,c) -> { if (c.get().getSender() != null) RankServer.save(c.get().getSender(),p); }).add();
  CHANNEL.messageBuilder(RankPayloads.Delete.class, 2, NetworkDirection.PLAY_TO_SERVER)
   .encoder((p,b) -> RankPayloads.Delete.CODEC.encode(b,p)).decoder(RankPayloads.Delete.CODEC::decode)
   .consumerMainThread((p,c) -> { if (c.get().getSender() != null) RankServer.delete(c.get().getSender(),p); }).add();
 }
}
