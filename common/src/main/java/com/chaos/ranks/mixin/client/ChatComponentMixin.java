package com.chaos.ranks.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.client.gui.components.ChatComponent;
import com.chaos.ranks.client.ChatSpacing;

/** Keep drawing, background, scrolling and click capture on the same row height in 26.1. */
@Mixin(ChatComponent.class)
abstract class ChatComponentMixin {
 @ModifyConstant(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", constant = @Constant(intValue = 9))
 private int chaosranks$drawingHeight(int vanilla) { return ChatSpacing.ROW_HEIGHT; }

 // Forge and NeoForge recompile Minecraft: javac folds the row calculation to a double literal.
 // Fabric keeps Mojang's bytecode, where this literal is absent and the integer local is reused.
 @ModifyConstant(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V", constant = @Constant(doubleValue = 9.0), require = 0)
 private double chaosranks$recompiledDrawingHeight(double vanilla) { return ChatSpacing.ROW_HEIGHT; }

 @ModifyConstant(method = "getLineHeight", constant = @Constant(doubleValue = 9.0))
 private double chaosranks$interactionHeight(double vanilla) { return ChatSpacing.ROW_HEIGHT; }
}
