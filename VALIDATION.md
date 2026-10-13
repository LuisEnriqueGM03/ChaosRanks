# Validación de los ports de Chaos Ranks 1.0.1

Minecraft 1.21.1, Java 21, NeoForge 21.1.257 y Forge 52.1.16.

| Comprobación | NeoForge | Forge |
|---|---|---|
| Compilación del mod y fuentes | Aprobada | Aprobada |
| Regresiones de persistencia, validación, revisiones y codecs | 82 comprobaciones | 82 comprobaciones |
| Chat, Tab, etiquetas y cuatro pestañas | Aprobada | Aprobada |
| Guardado por red y renombrado | Aprobada | Aprobada |
| Menú a 342 × 256 unidades y clic escalado | Aprobada | Aprobada |
| Inicio preliminar del cargador en modo servidor | Aprobado | Aprobado |

Se revisaron capturas de ambos cargadores y se inspeccionan los JAR con `python tools/validate-ports.py`: identificador, versión, autor, rangos de dependencia, Java 21, proveedor de plataforma, recursos, licencia y ausencia de clases de otros cargadores.

Las pruebas de comportamiento usan un servidor integrado y una copia aislada del mundo `selftest`. El modo servidor preliminar termina al inicializar sus ajustes o en la comprobación de la EULA: no se aceptó ese acuerdo ni se abrió un servidor de juego. No se hizo una sesión con dos clientes independientes en un servidor dedicado.

Forge requiere reunir clases y recursos en un mismo directorio para sus lanzamientos de desarrollo. Se agregó `pack.mcmeta` para recursos y datos de Minecraft 1.21.1 y se fijó la versión mínima de Mixin. Las autopruebas abren el mundo desde la pantalla inicial, después de completar la inicialización del cargador; Quick Play evitaba parte de esa inicialización en el entorno Forge probado.

Las pruebas no muestran errores del mod tras esas correcciones. Permanecen avisos del entorno de desarrollo sobre bibliotecas, sonidos y paquetes de la copia del mundo anterior. No se alteraron los mundos originales.

## Reproducción

- NeoForge: `gradlew -Ploader=neoforge build` y `gradlew -Ploader=neoforge :neoforge:runSelftestSmall`.
- Forge: `gradlew-forge :forge:build` y `gradlew-forge :forge:runSelftestSmall`.
- Usar Java 21 y la copia de prueba en `<cargador>/run-1.21.1/saves/selftest`.
- En Windows usar `.bat`; en otros sistemas dar permiso de ejecución a los lanzadores.

Las ramas seleccionan su cargador mediante `default_loader`. El lanzador específico de Forge utiliza Gradle 8.12; NeoForge y Fabric usan Gradle 9.7.1. Los archivos publicados se identifican por cargador, Minecraft y versión del mod.

El adaptador Fabric del módulo compartido también se comprueba con su herramienta Loom 1.18 (Java 25 para ejecutar Gradle, objetivo Java 21). La rama Fabric publicada anteriormente conserva su estructura original.
