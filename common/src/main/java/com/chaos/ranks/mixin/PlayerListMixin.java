package com.chaos.ranks.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaos.ranks.rank.RankTeams;
import net.minecraft.server.players.NameAndId;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

/** Al dar o quitar OP se recalcula la etiqueta: /op pone el rango de OP y /deop devuelve al de antes. */
@Mixin(PlayerList.class)
abstract class PlayerListMixin {

	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = {"op", "deop"}, at = @At("TAIL"))
	private void chaosranks$resync(NameAndId profile, CallbackInfo ci) {
		RankTeams.sync(server);
	}
}
