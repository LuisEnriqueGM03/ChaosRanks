package com.chaos.ranks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.chaos.ranks.server.RankChat;

import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/** Los mensajes de los jugadores usan el tipo de chat del mod ("%s %s") en vez de "<%s> %s". */
@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {

	@ModifyArg(method = "broadcastChatMessage", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/network/chat/ChatType;bind(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/network/chat/ChatType$Bound;"),
			index = 0)
	private ResourceKey<ChatType> chaosranks$chatType(ResourceKey<ChatType> original) {
		return RankChat.CHAT_TYPE;
	}
}
