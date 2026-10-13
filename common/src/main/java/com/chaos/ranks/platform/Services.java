package com.chaos.ranks.platform;
import java.util.ServiceLoader;
public final class Services {
 public static final Platform PLATFORM = ServiceLoader.load(Platform.class, Services.class.getClassLoader()).findFirst()
  .orElseThrow(() -> new IllegalStateException("Missing ChaosRanks loader integration"));
 private Services() {}
}
