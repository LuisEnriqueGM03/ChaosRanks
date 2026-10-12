package com.chaos.ranks.client;

/** Shared rendering/input scale, independent of Minecraft for regression checks. */
public final class MenuGeometry {
 private MenuGeometry() {}
 public static double scale(int width, int height) {
  return Math.max(0.01, Math.min(1.0, Math.min(width / 428.0, height / 244.0)));
 }
 public static int scissorStart(int coordinate, double scale) { return (int) Math.floor(coordinate * scale); }
 public static int scissorEnd(int coordinate, double scale) { return (int) Math.ceil(coordinate * scale); }
}
