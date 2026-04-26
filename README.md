# 👁️ DiolezSpectate

**High-performance CPVP spectating plugin for Spigot 1.21**  
*Strict POV locking · Request system · Zero free-roam*

![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-gold?style=for-the-badge)
![Spigot](https://img.shields.io/badge/Spigot-1.21.4-orange?style=for-the-badge&logo=minecraft)
![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

> Made with ❤️ by **Diolezz** — Built for private Crystal PvP tier servers.

</div>

---

## ✨ Features

- 🔒 **Locked POV** — Spectator is forced into the target's camera. Shift-to-exit is fully blocked.
- 📡 **Request System** — Clickable accept/deny notifications. No surprise spectating.
- 🌀 **Teleport Tracking** — If the target pearl throws or warps, the spectator instantly snaps to them.
- 🏠 **Location Restore** — On session end, the spectator is returned to their exact original location & gamemode.
- 🔌 **Auto-End** — Session ends automatically if the target disconnects.
- 🧹 **Clean Memory** — All HashMaps and scheduler tasks are wiped on session end and plugin disable.
- 🎨 **Branded Startup** — Large ASCII art banner printed to console on enable.

---

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/spectate request <player>` | Send a spectate request to a player | `diolezspectate.use` |
| `/spectate accept` | Accept an incoming spectate request | `diolezspectate.use` |
| `/spectate deny` | Deny an incoming spectate request | `diolezspectate.use` |
| `/spectate leave` | Exit your current spectate session | `diolezspectate.use` |

**Alias:** `/spec` works for all commands.

---

## 🔐 Permissions

| Node | Default | Description |
|------|---------|-------------|
| `diolezspectate.use` | `op` | Access to all DiolezSpectate commands |

To grant to all players via LuckPerms:
```
/lp group default permission set diolezspectate.use true
```

---

## ⚙️ Installation

1. Download the latest `DiolezSpectate-1.0-SNAPSHOT.jar` from [Releases](../../releases)
2. Drop it into your server's `/plugins` folder
3. Restart the server
4. Done — no config required

**Requirements:**
- Spigot / Paper `1.21.x`
- Java `21+`

---

## 🔧 How It Works

### POV Lock (Double-Layer)

Vanilla Minecraft lets a spectator press **Shift** to exit a player's body.  
DiolezSpectate defeats this with two layers:

**Layer 1 — Event Block**  
`PlayerToggleSneakEvent` is cancelled at `HIGHEST` priority for any locked spectator.

**Layer 2 — Repeating Re-lock Task (every 2 ticks / 100ms)**  
A scheduler task continuously calls `setSpectatorTarget(target)`.  
This handles pearl throws, warp teleports, and any other TP that would reset the camera.

### Session State Machine

```
[IDLE] ──/spectate request──▶ [PENDING] ──accept──▶ [SPECTATING]
                                   │                      │
                                 deny               leave / target disconnect
                                   │                      │
                                [IDLE]               [IDLE] (restored)
```

### Memory Layout

```
SpectateManager
├── sessions        Map<spectatorId, SpectateSession>   active sessions
├── spectatorOf     Map<targetId, spectatorId>          reverse lookup
├── pendingRequests Map<targetId, spectatorId>          unanswered requests
└── lockTasks       Map<spectatorId, BukkitTask>        repeating lock tasks
```

All maps are fully cleared on session end, player disconnect, and plugin disable.

---

## 🏗️ Building from Source

```bash
# 1. Clone the repo
git clone https://github.com/Diolezz/DiolezSpectate.git
cd DiolezSpectate

# 2. Build Spigot API locally (first time only)
java -jar BuildTools.jar --rev 1.21.4

# 3. Compile
mvn clean package

# 4. JAR is at:
#    target/DiolezSpectate-1.0-SNAPSHOT.jar
```

---

## 📁 Project Structure

```
src/main/java/me/diolezz/diolezspectate/
├── DiolezSpectate.java          Main class — onEnable / onDisable
├── commands/
│   └── SpectateCommand.java     All subcommands + tab completion
├── listeners/
│   └── SpectateListener.java    Sneak block, gamemode guard, disconnect handler
├── managers/
│   └── SpectateManager.java     Session logic + POV lock scheduler
├── model/
│   └── SpectateSession.java     Session data (location, gamemode, UUIDs)
└── util/
    └── Messages.java            Colour & prefix utilities
```

---

## 📜 License

MIT License — feel free to fork and modify.  
Credit to **Diolezz** appreciated but not required.

---



**DiolezSpectate** — *by Diolezz*

</div>
