package com.chaos.ranks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
public final class ChaosRanks {
 public static final String MOD_ID = "chaosranks";
 public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
 private ChaosRanks() {}
 public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
 public static void started(MinecraftServer server) { RankStore.load(server); RankTeams.sync(server); }
 public static void joined(ServerPlayer player) { RankTeams.sync(player.level().getServer(), player); }
}
