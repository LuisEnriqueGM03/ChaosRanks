## Chaos Ranks 1.0.1 — Forge and NeoForge ports

- Mod author: **Goddark83**.
- Minecraft 1.21.1 ports for NeoForge 21.1.257 and Forge 52.1.16, with Java 21.
- Shared rank, storage, badge, UI and regression code in `common`; native loader integrations in separate modules.
- Native command, lifecycle, player join, chat and client tick events for each loader.
- Native NeoForge payloads and a versioned Forge channel with explicit packet directions and main-thread handling.
- Preserve the atomic store, backup recovery, stale-editor protection and adaptive GUI from Fabric 1.0.1.
- Forge-specific Gradle wrapper to support its development toolchain.
- Install the matching loader JAR on server and clients; Fabric API is not required for these ports.

## Chaos Ranks 1.0.1

- Save ranks and assignments together in an atomic snapshot with a recoverable backup.
- Migrate and preserve version 1.0.0 files; damaged data disables edits instead of being overwritten.
- Validate loaded ranks and reject null or malformed save messages.
- Report persistence errors and commit in-memory changes only after saving succeeds.
- Reject stale editor changes to prevent conflicting administrator edits.
- Split large editor lists into bounded network messages; update server and clients together.
- Fit the editor to smaller viewports and clamp list scrolling after updates.
- Use locale-independent IDs and translations, translated editor titles and new-rank labels.
- Strip control characters from chat separators and preserve Unicode boundaries.
- Save bulk player assignments once per command.
- Add executable regression tests to the build, include the license in the jar, and fix screenshot links.
- Validate upload release types and use unique temporary metadata files with cleanup.

## Chaos Ranks 1.0.0

First release.

- Pixel-art rank badges next to player names in chat, tab list and nametags.
- In-game editor (`/rank`) with live preview: text, 144 icons, background/text/icon/frame colors, bold, priority.
- Per-rank chat format: name color (any RGB) and bold, message color, custom separator. No more `<>` brackets.
- Default **PLAYER** rank for everyone and automatic **OP** rank with `/op` / `/deop`.
- Commands: `/rank give`, `/rank remove`, `/rank delete`.
- English and Spanish translations.
