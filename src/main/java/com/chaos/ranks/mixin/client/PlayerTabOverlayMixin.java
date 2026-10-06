package com.chaos.ranks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.chaos.ranks.badge.BadgeRenderer;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

/** En el Tab las filas miden 9 px: las etiquetas se pintan con la fuente compacta. */
@Mixin(PlayerTabOverlay.class)
abstract class PlayerTabOverlayMixin {

	@Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
	private void chaosranks$compact(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
		cir.setReturnValue(BadgeRenderer.compact(cir.getReturnValue()));
	}
}
