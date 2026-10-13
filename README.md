# Chaos Ranks 1.0.2

Mod para Fabric, NeoForge y Forge (Minecraft 26.2) que añade etiquetas de rango en pixel art al lado del nombre del jugador. Las etiquetas se ven en el chat, en el Tab y encima de la cabeza.

Tiene que estar instalado en el servidor **y** en los clientes: el cliente aporta la fuente con los gráficos y la pantalla para escribir el texto.

## Uso (requiere OP)

| Comando | Qué hace |
|---|---|
| `/rank` | Abre el menú con todos los rangos y el botón para crear uno nuevo |
| `/rank give <jugadores> <rango>` | Da un rango (cada jugador tiene uno) |
| `/rank remove <jugadores>` | Quita el rango |
| `/rank delete <rango>` | Borra el rango |

`/rank` abre un menú propio del mod (no un cofre). A la izquierda está la lista de rangos y arriba la vista previa en grande, junto con cómo queda en el chat. Tiene cuatro pestañas:
- **Etiqueta**: texto, negrita y prioridad (cuanto más alta, antes sale en el Tab).
- **Icono**: 144 iconos propios. Se cambia de página con la rueda del ratón.
- **Colores**: fondo, texto, icono y marco (o sin marco). Hay paleta y campo hex.
- **Chat**: separador (`»`, `->`…) y colores libres (paleta o hex) del nombre, el mensaje y el separador. El nombre también puede ir en negrita, y se ve igual en el chat, en el Tab y encima de la cabeza.

El chat queda como `ETIQUETA Nombre » mensaje`, sin los `<>` de vanilla.

**Rangos de serie:** la primera vez que se carga cada mundo se crean dos rangos:
- **PLAYER** (por defecto): lo tienen todos los que no tienen otro rango, también quien entra por primera vez.
- **OP** (para OPs): se pone solo con `/op` y vuelve al rango por defecto con `/deop`.

Desde la pestaña Etiqueta se puede marcar cualquier otro rango como "Por defecto" o "Para OPs" (solo uno de cada). El orden es: rango dado a mano con `/rank give` → rango de OP (si es OP) → rango por defecto.

**Renombrar:** si cambias el texto de un rango y guardas, su identificador pasa a ser el nuevo (OWNER → OP pasa de `owner` a `op`). Los jugadores que lo tenían se mueven al identificador nuevo.

**Idiomas:** inglés y español (todas las variantes `es_*`). El menú y los mensajes salen en el idioma del cliente.

Los rangos y las asignaciones se guardan juntos en `<mundo>/chaosranks/store.json`, con respaldo automático en `store.json.bak`. La actualización migra `ranks.json`, `players.json` y `state.json` de 1.0.0 y conserva esos archivos originales; después de migrar, se utiliza únicamente `store.json`.

Guarda una copia del mundo antes de actualizar. Actualiza el servidor y los clientes a 1.0.1 para utilizar el nuevo protocolo del editor. Los archivos originales de 1.0.0 no reciben los cambios nuevos: volver a esa versión requiere restaurar el respaldo del mundo anterior.

Si el archivo principal está dañado, se intenta recuperar el último respaldo válido y se conserva el principal como `store.corrupt-<uuid>.json`. El respaldo puede contener el estado anterior al último cambio. Si no hay datos recuperables, la edición queda bloqueada y el registro del servidor indica el problema; repara los archivos y reinicia. Los errores de guardado se muestran al administrador y no aplican el cambio en memoria.

El menú se adapta al tamaño de la interfaz. Las listas grandes se transmiten en fragmentos, con un límite total de 4 Mi caracteres de JSON. Si los rangos cambian mientras otro administrador edita, se rechaza la edición antigua; vuelve a abrir `/rank` para cargar el estado actual.

## Cómo funciona

La etiqueta es un texto normal que usa la fuente `chaosranks:badge`, así que vale en cualquier sitio donde se pinte texto. Se dibuja en tres capas, retrocediendo con espacios negativos:

1. borde,
2. fondo con el hueco de cada letra,
3. letras e icono.

Las capas no comparten píxeles para que no parpadeen en el mundo 3D. La etiqueta mide 11 px, con 1 px de aire encima y debajo del icono. El cliente sube 4 px el alto de las líneas del chat para que, contando la sombra, quede 1 px libre entre una línea y otra. El Tab usa `chaosranks:badge_compact`, de 9 px, porque sus filas son más bajas. El formato del chat usa el tipo de chat `chaosranks:rank` más un decorador del mensaje. Para que aparezca junto al nombre, cada rango crea un equipo del scoreboard (`chaosrank_*`) con la etiqueta como prefijo.

Ojo: si otro mod o plugin mete a los mismos jugadores en otros equipos, chocarán.

## Ports para Minecraft 26.2

Autor: **Goddark83**. Java 25 en cliente, servidor y desarrollo.

- Fabric: Loader 0.19.5 y Fabric API 0.161.0 para 26.2. Rama `fabric-26.2`.
- Forge: 65.1.3 para 26.2. Rama `forge-26.2`.
- NeoForge: 26.2.0.90 para 26.2. Rama `neoforge-26.2`.

Instala el JAR correspondiente en servidor y clientes. Forge y NeoForge no requieren Fabric API. Los rangos mantienen el formato `store.json` de Chaos Ranks 1.0.1.

Se adaptaron el dibujo del men� al sistema de extracci�n, colores ARGB, fuentes FontDescription, eventos de rat�n con modificadores, permisos de administraci�n y los eventos nativos de los cargadores. Los recursos usan el formato 88 de Minecraft 26.2.

### Desarrollo

Gradle 9.7.1 y Java 25. `gradlew -Ploader=fabric build`, `gradlew -Ploader=forge build` o `gradlew -Ploader=neoforge build`. En Windows usar `.bat`. Cada rama selecciona su cargador predeterminado.

Los JAR est�n en `<cargador>/build/libs`. Las autopruebas `:fabric:runSelftestSmall`, `-PselftestSmall :forge:runClient` y `:neoforge:runSelftestSmall` crean mundos de desarrollo y verifican el editor y su guardado por red. Modifican exclusivamente esos mundos.

El resultado y los l�mites de las pruebas se documentan en VALIDATION.md.

## Correcci�n del chat en 26.2

La versi�n 1.0.2 usa filas de 14 p�xeles para etiquetas de 11, con 3 p�xeles de separaci�n. El dibujo, fondos, desplazamiento y captura de clics comparten la misma altura, respetando el ajuste de espaciado de Minecraft.
