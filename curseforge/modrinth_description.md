# Show who's who, in pixel-art style.

**Chaos Ranks** adds custom **pixel-art rank badges** next to player names: in **chat**, in the **tab list** and **above their heads**. Design every badge in-game from a custom menu with a **live preview**: no config files, no resource packs to send around, no restarts.

OWNER, ADMIN, VIP+, YOUTUBE, BUILDER… whatever ranks your server needs, with your own colors, frames and icons. Built for **Fabric 1.21.1**.

![Rank badges in chat](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/chat.png)

---

## ✨ Features

### 🏷️ Badges everywhere a name shows up
- **Chat**: `[BADGE] Name » message`. The vanilla `<>` brackets are gone.
- **Tab list**: a compact version of the badge that fits the row perfectly, sorted by rank priority.
- **Nametags**: the same badge floats above the player's head.
- Badges are crisp pixel art at any GUI scale, with a subtle 3D shading and drop shadow.

![Rank badge in the tab list](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/tab.png)

![Rank badge above a player's head](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/nametag.png)

### 🎨 A real in-game editor
Type `/rank` and a custom menu opens (not a chest!). On the left is the list of ranks, at the top a big live preview of the badge and how it looks in chat, and below it four tabs:
- **Badge**: text, bold, priority, and whether it's the *default* rank or the *OP* rank.
- **Icon**: pick from **144 hand-made pixel icons**.
- **Colors**: background, text, icon and frame (or no frame). Pick from the palette or type any hex color.
- **Chat**: the player's name color (any RGB color, not just the 16 vanilla ones), bold name, message color, and a custom separator (`»`, `->`, `|`…) with its own color.

![Rank editor - Badge tab](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/menu_label.png)

![Rank editor - Icon tab](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/menu_icon.png)

### 🖼️ 144 pixel icons
Crowns, swords, shields, hammers, gems, diamonds, emeralds, creepers, TNT, dragons, hearts, stars, skulls, wizards, ninjas, robots… plus social icons for **YouTube, Twitch, Discord, TikTok, Instagram, Twitter/X, Kick, Spotify, GitHub and Reddit**. They're all drawn for the badge size, so they stay sharp.

![Rank editor - Colors tab](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/menu_colors.png)

### 💬 Chat format per rank
Every rank has its own chat style. Make your owners shine with a **bold gold name** and yellow text, and keep regular players clean and simple. Colors you choose for the name also apply to the **tab list** and the **nametag**.

![Rank editor - Chat tab](https://raw.githubusercontent.com/LuisEnriqueGM03/ChaosRanks/main/curseforge/menu_chat.png)

### ⚙️ Works out of the box
- Comes with two ranks ready to go: **PLAYER**, the default for everyone (including new players on their first join), and **OP**.
- **Automatic OP rank**: `/op player` gives them the OP badge instantly, and `/deop` puts them back to their previous rank.
- Any rank can be marked as *default* or *for OPs* from the editor.
- Rename a rank and everybody who had it follows the change automatically.
- Ranks and assignments are saved together per world in `<world>/chaosranks/store.json`, with an automatic backup. Version 1.0.0 files are migrated and preserved.
- **English and Spanish** translations.

---

## 📜 Commands
All commands require operator permissions (level 2).

| Command | What it does |
|---|---|
| `/rank` | Opens the rank editor |
| `/rank give <players> <rank>` | Gives a rank (overrides the default and OP rank) |
| `/rank remove <players>` | Removes the manual rank, so the player falls back to the OP or default rank |
| `/rank delete <rank>` | Deletes a rank |

Which rank a player shows: **rank given with /rank give** → **OP rank** (if they're an operator) → **default rank**.

---

## 📦 Installation
- Requires **Fabric Loader** and **[Fabric API](https://modrinth.com/mod/fabric-api)** for Minecraft **1.21.1**.
- Install version **1.0.1 on the server and on every client** for the updated editor protocol. The client provides the pixel font, the editor menu and the chat spacing.
- Works in singleplayer too.

## ⚠️ Compatibility notes
- Badges use scoreboard teams (`chaosrank_*`) to show up next to names. Other mods or plugins that put the same players into their own teams may conflict.
- Chat lines are slightly taller (+4 px) so badges on consecutive lines don't touch.

---

Made with ❤️ by **Goddark83**. Feedback and ideas are welcome!
