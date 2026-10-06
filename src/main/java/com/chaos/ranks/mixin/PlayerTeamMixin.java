package com.chaos.ranks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.chaos.ranks.rank.RankTeams;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.scores.PlayerTeam;

/**
 * Nombre del jugador con color libre (no solo los 16 de los equipos): en los equipos del mod, el estilo del
 * nombre del equipo (que se sincroniza con los clientes) es el estilo del nombre del jugador.
 * Corre en servidor (chat) y en cliente (Tab y nametag).
 */
@Mixin(PlayerTeam.class)
abstract class PlayerTeamMixin {

	@Inject(method = "getFormattedName", at = @At("HEAD"), cancellable = true)
	private void chaosranks$styledName(Component name, CallbackInfoReturnable<MutableComponent> cir) {
		PlayerTeam team = (PlayerTeam) (Object) this;
		if (!team.getName().startsWith(RankTeams.PREFIX)) {
			return;
		}
		Style style = team.getDisplayName().getStyle();
		cir.setReturnValue(Component.empty()
				.append(team.getPlayerPrefix())
				.append(name.copy().withStyle(style::applyTo))
				.append(team.getPlayerSuffix()));
	}
}
