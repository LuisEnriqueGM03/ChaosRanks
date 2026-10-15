## Chaos Ranks 1.0.2 - Minecraft 26.3

Author: **Goddark83**. Java 25.

- Fabric, Forge and NeoForge ports for Minecraft 26.3.
- Updated screen/HUD access, resource format 97.1 and data format 121.0 and client integration.
- Fabric verified in-game: 14-pixel chat rows for 11-pixel rank badges, with a 3-pixel gap; custom spacing, scaled editor input and network edits also passed.
- Preserves Minecraft chat line-spacing settings.
- Atomic storage and backup recovery, revision checks and all 144 icons preserved.
- Install the matching loader JAR on server and clients; Fabric requires Fabric API.

Validation: all three loaders compile and pass 82 regression checks. Forge and NeoForge are **beta**: their in-game and visual chat checks remain pending because the test PC crashes in AMD atio6axx.dll during OpenGL startup. See VALIDATION.md.
