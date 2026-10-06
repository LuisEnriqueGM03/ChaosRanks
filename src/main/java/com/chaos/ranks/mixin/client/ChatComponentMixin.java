package com.chaos.ranks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.components.ChatComponent;

/** Líneas del chat 4 px más altas: las etiquetas miden 11 px y su sombra 1 más, así queda 1 px libre entre una y otra. */
@Mixin(ChatComponent.class)
abstract class ChatComponentMixin {

	private static final int EXTRA = 4;

	@Inject(method = "getLineHeight", at = @At("RETURN"), cancellable = true)
	private void chaosranks$taller(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(cir.getReturnValueI() + EXTRA);
	}
}
