package com.chaos.ranks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.chaos.ranks.server.ChatNameRewriter;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Nombre con rango en los mensajes de chat que otros mods reenvían como mensaje de sistema (ver ChatNameRewriter). */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin {

	@ModifyVariable(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), argsOnly = true)
	private Component chaosranks$rankedName(Component message) {
		return ChatNameRewriter.rewrite(message);
	}
}
