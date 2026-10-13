# Validación de Chaos Ranks 1.0.1 para Minecraft 1.20.1

Autor: Goddark83. Fabric Loader 0.19.5 / Fabric API 0.92.6, Forge 47.4.0 y NeoForge 47.1.106.

Los tres cargadores se compilan con objetivo Java 17. Se ejecutan 87 comprobaciones por cargador: persistencia, recuperación, migración, revisiones, límites de paquetes, Unicode y escala de interfaz. Se inspeccionan los JAR finales remapeados, metadatos, proveedores de plataforma, refmaps de los cinco mixins, recursos e iconos.

Las tres autopruebas dentro de Minecraft terminaron correctamente. Usan mundos nuevos de desarrollo; comprueban el guardado por red y renombrado OWNER a JEFE, abren cuatro pestañas del editor, capturan chat, Tab y etiquetas, y verifican un clic real en el botón de negrita en una interfaz de 342 × 256 unidades. Se revisaron capturas de los tres cargadores.

No se realizó una sesión con dos clientes independientes en servidor dedicado ni una ejecución separada de los JAR de producción en un lanzador externo. Los refmaps y el remapeo de producción se validan en los archivos publicados. Los mundos originales no se modificaron.

## Reproducción

`gradlew -Ploader=fabric build`, `gradlew -Ploader=forge build` o `gradlew -Ploader=neoforge build`. En Windows usar `.bat`.

Gradle 9.7.1; Java 21 para las herramientas Forge/NeoForge y Java 25 para Loom de Fabric. Bytecode publicado: Java 17.

Autopruebas: `gradlew -Ploader=forge :forge:runSelftestSmall`; reemplazar ambas referencias al cargador para las otras variantes. Se crea `selftest` si no existe en `<cargador>/run-1.20.1/saves`; solo usar esa carpeta para mundos de prueba. La autoprueba modifica los rangos de ese mundo.
