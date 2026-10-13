package com.chaos.ranks.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
final class LegacySprites {
 static void blit(GuiGraphics g, ResourceLocation sprite, int x, int y, int w, int h) {
  int[] size = switch(sprite.getPath()) { case "button" -> new int[] {20,20,3}; case "button_disabled" -> new int[] {20,20,3}; case "button_hover" -> new int[] {20,20,3}; case "button_selected" -> new int[] {20,20,3}; case "cell" -> new int[] {16,16,2}; case "header" -> new int[] {16,14,3}; case "inset" -> new int[] {16,16,2}; case "panel" -> new int[] {48,48,8}; default -> throw new IllegalArgumentException(sprite.toString()); };
  ResourceLocation texture = new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
  int b = Math.min(size[2], Math.min(w/2,h/2));
  int[] dx={0,b,w-b,w}, dy={0,b,h-b,h}, sx={0,b,size[0]-b,size[0]}, sy={0,b,size[1]-b,size[1]};
  for(int i=0;i<3;i++) for(int j=0;j<3;j++) g.blit(texture,x+dx[i],y+dy[j],dx[i+1]-dx[i],dy[j+1]-dy[j],sx[i],sy[j],sx[i+1]-sx[i],sy[j+1]-sy[j],size[0],size[1]);
 }
}
