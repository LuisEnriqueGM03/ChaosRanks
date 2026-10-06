# Chaos Ranks

Mod de Fabric (Minecraft 1.21.1) que añade etiquetas de rango en pixel art al lado del nombre del jugador. Las etiquetas se ven en el chat, en el Tab y encima de la cabeza.

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

Los datos se guardan en `<mundo>/chaosranks/ranks.json` y `players.json`.

## Cómo funciona

La etiqueta es un texto normal que usa la fuente `chaosranks:badge`, así que vale en cualquier sitio donde se pinte texto. Se dibuja en tres capas, retrocediendo con espacios negativos:

1. borde,
2. fondo con el hueco de cada letra,
3. letras e icono.

Las capas no comparten píxeles para que no parpadeen en el mundo 3D. La etiqueta mide 11 px, con 1 px de aire encima y debajo del icono. El cliente sube 4 px el alto de las líneas del chat para que, contando la sombra, quede 1 px libre entre una línea y otra. El Tab usa `chaosranks:badge_compact`, de 9 px, porque sus filas son más bajas. El formato del chat usa el tipo de chat `chaosranks:rank` más un decorador del mensaje. Para que aparezca junto al nombre, cada rango crea un equipo del scoreboard (`chaosrank_*`) con la etiqueta como prefijo.

Ojo: si otro mod o plugin mete a los mismos jugadores en otros equipos, chocarán.

## Desarrollo

- `python tools/gen_font.py` regenera las texturas de la fuente y `badge_meta.json` a partir del ASCII art del script. Ahí se añaden letras o iconos.
- `python tools/gen_gui.py` regenera las texturas del menú (marrón con contorno dorado).
- `./gradlew build` genera el jar en `build/libs/`.
- `./gradlew runSelftest` crea rangos de ejemplo y guarda capturas en `run/screenshots/selftest_*.png`. Necesita el mundo `run/saves/selftest`.
