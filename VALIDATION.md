# Validación de Chaos Ranks 1.0.1 para Minecraft 26.1

Autor: Goddark83. Java 25. Fabric Loader 0.19.5 / Fabric API 0.145.1, Forge 62.0.9 y NeoForge 26.1.0.19-beta.

Cada cargador ejecuta 82 comprobaciones de persistencia, recuperación, migración, revisiones, paquetes grandes, Unicode y escala. Los tres cargadores se prueban dentro de Minecraft con servidor integrado y mundos nuevos de desarrollo: guardado y renombrado por red, chat, Tab, etiquetas, cuatro pestañas del editor y un clic real en el menú pequeño a 342 × 256 unidades. Se revisan capturas del menú de cada cargador.

Se adaptaron Identifier, FontDescription, permisos de gamemaster, NameAndId, GUI por extracción, eventos del ratón con modificadores, eventos de Forge y envío cliente de NeoForge. La nueva extracción requiere ARGB opaco para textos y composición explícita del panel dentro de su pose; se desactiva la sombra de los glifos de las etiquetas para evitar interferencias entre capas.

Los JAR se inspeccionan por metadatos, autor, objetivo Java 25, recursos formato 84, proveedor de plataforma y 144 iconos. Forge utiliza compatibilidad Mixin JAVA_21 debido al enum disponible en su Mixin integrado; el mod y Minecraft siguen requiriendo Java 25.

No se hizo una sesión con dos clientes independientes en servidor dedicado ni una ejecución separada en un lanzador externo. No se modificaron los mundos originales.

## Reproducción

Gradle 9.7.1 / Java 25. Compilar con `gradlew -Ploader=fabric build`, `gradlew -Ploader=forge build` o `gradlew -Ploader=neoforge build`; en Windows usar `.bat`.

Fabric y NeoForge: `gradlew -Ploader=fabric :fabric:runSelftestSmall` o `gradlew -Ploader=neoforge :neoforge:runSelftestSmall`. Forge: `gradlew -Ploader=forge -PselftestSmall :forge:runClient`.

Solo usar las carpetas de desarrollo para mundos de prueba: las autopruebas crean y modifican `selftest`. Los JAR finales están en `<cargador>/build/libs`.
