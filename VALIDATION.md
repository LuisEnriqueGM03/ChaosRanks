# Validación de Chaos Ranks 1.0.2 para Minecraft 26.3

Autor: Goddark83. Java 25. Fabric Loader 0.19.5 / Fabric API 0.162.0+26.3, Forge 66.0.10 y NeoForge 26.3.0.76.

## Resultados de esta versión

| Comprobación | Fabric | Forge | NeoForge |
|---|---|---|---|
| Compilación y 82 comprobaciones de regresión | Correcto | Correcto | Correcto |
| Inspección del JAR y sus metadatos | Correcto | Correcto | Correcto |
| Servidor integrado, editor y guardado por red | Correcto | Pendiente | Pendiente |
| Chat, captura y separación visual | Correcto | Pendiente | Pendiente |

Fabric pasó la autoprueba dentro de Minecraft: chat, Tab, etiquetas, cuatro pestañas, creación, guardado, renombrado y eliminación por red, y clic real en el editor a 342 × 256 unidades. La altura del chat devuelve 14 píxeles y 21 con espaciado 0.5. La captura de tres etiquetas consecutivas confirma 42 píxeles físicos por fila con escala ×3, equivalentes a 14 de interfaz y 3 de separación entre etiquetas de 11.

Forge y NeoForge compilan y superan las 82 comprobaciones de persistencia, recuperación, migración, revisiones, paquetes grandes, Unicode y escala. Sus clientes de desarrollo se bloquean en la biblioteca gráfica AMD atio6axx.dll con acceso inválido durante el arranque de OpenGL. No se pudo completar su prueba dentro del juego ni confirmar visualmente el chat en 26.3. Se distribuyen como beta por esta limitación; necesitan validación en otro entorno gráfico. No se atribuye el fallo al mod ni se garantiza que sea exclusivamente del controlador.

Los JAR se inspeccionan por autor, versión, objetivo Java 25, proveedor de plataforma, 144 iconos y ausencia de clases de otros cargadores. El paquete conjunto cubre los formatos de recursos 97.1 y datos 121.0 de Minecraft 26.3. La autoprueba usa el identificador actualizado del botón izquierdo de 26.3.

El mixin del chat conserva la corrección de filas de 14 píxeles y cubre las constantes int y double que producen las herramientas de cada cargador. El resultado de una captura de Fabric no demuestra el dibujo de Forge o NeoForge.

No se realizó una sesión con dos clientes independientes en servidor dedicado ni una ejecución separada en un lanzador externo. No se modificaron los mundos originales.

## Reproducción

Gradle 9.7.1 / Java 25. Compilar con `gradlew -Ploader=fabric build`, `gradlew -Ploader=forge build` o `gradlew -Ploader=neoforge build`; en Windows usar `.bat`.

Fabric y NeoForge: `gradlew -Ploader=fabric :fabric:runSelftestSmall` o `gradlew -Ploader=neoforge :neoforge:runSelftestSmall`. Forge: `gradlew -Ploader=forge -PselftestSmall :forge:runClient`.

Las autopruebas crean y modifican mundos `selftest` exclusivamente en carpetas de desarrollo. Los JAR finales están en `<cargador>/build/libs`.

Inspección de los tres JAR: `python tools/validate-ports.py`. Medición de la captura de Fabric: `python tools/check-chat-screenshot.py run-26.3-small/screenshots/selftest_small_chat.png`.
