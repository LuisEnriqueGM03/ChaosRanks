package com.chaos.ranks.client;
import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.net.RankPayloads;
import net.minecraft.client.Minecraft;
public final class ChaosRanksClient {
 private static final RankPayloads.Assembler ASSEMBLER = new RankPayloads.Assembler();
 private ChaosRanksClient() {}
 public static void disconnected() { ASSEMBLER.clear(); }
 public static void receive(RankPayloads.OpenManager payload) {
  Minecraft client = Minecraft.getInstance();
  client.execute(() -> {
   try {
    var ranks = ASSEMBLER.accept(payload);
    if (ranks == null) return;
    if (client.screen instanceof RankManagerScreen screen) screen.update(ranks, payload.selectId(), payload.revision());
    else client.setScreen(new RankManagerScreen(ranks, payload.selectId(), payload.revision()));
   } catch (Exception e) { ASSEMBLER.clear(); ChaosRanks.LOGGER.warn("Invalid rank manager response", e); }
  });
 }
 public static void tick() { if (SelfTest.enabled()) SelfTest.tick(Minecraft.getInstance()); }
}
