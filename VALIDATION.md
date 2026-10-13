# Validación de Chaos Ranks 1.0.1 para Minecraft 1.21.4

Autor: Goddark83. Java 21. Fabric API 0.119.4, NeoForge 21.4.158 y Forge 54.1.14.

El build de cada cargador ejecuta 82 comprobaciones de persistencia, revisión, validación de paquetes, listas grandes, Unicode e interfaz. Las autopruebas del juego crean mundos nuevos de desarrollo y comprueban el renombrado por red, chat, Tab, etiquetas y las cuatro pestañas del editor. El modo pequeño verifica un clic real escalado a 342 × 256 unidades. Se inspeccionan los recursos, metadatos, Java y proveedores de plataforma de los JAR finales.

No se realizó una sesión con dos clientes independientes en servidor dedicado ni una ejecución separada en un lanzador externo. Los mundos originales no se modificaron.

NeoForge: Gradle 9.7.1 y Java 21. Forge: `gradlew-forge` selecciona Gradle 8.12 y Java 21. Fabric: Gradle 9.7.1 y Java 25 para las herramientas Loom, con objetivo Java 21 para el mod.

En Windows usar `.bat`. Compilar cada cargador por separado con `-Ploader=fabric`, `-Ploader=forge` o `-Ploader=neoforge`. Los JAR están en `<cargador>/build/libs`. Las autopruebas modifican únicamente el mundo `selftest` de desarrollo.

Se corrigió el recorte de listas y vista previa: en 1.21.4 GuiGraphics transforma los límites con la pose actual, por lo que no deben escalarse dos veces.
