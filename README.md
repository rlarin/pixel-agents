<h1 align="center">IT Crowd Pixel Agents</h1>

<h2 align="center" style="padding-bottom: 20px;">
  The game interface where AI agents build real things
</h2>

<div align="center" style="margin-top: 25px;">

[![npm version](https://img.shields.io/npm/v/it-crowd-pixel-agents?color=0183ff&style=flat)](https://www.npmjs.com/package/it-crowd-pixel-agents)
[![stars](https://img.shields.io/github/stars/rlarin/it-crowd-pixel-agents?logo=github&color=0183ff&style=flat)](https://github.com/rlarin/it-crowd-pixel-agents/stargazers)
[![license](https://img.shields.io/github/license/rlarin/it-crowd-pixel-agents?color=0183ff&style=flat)](https://github.com/rlarin/it-crowd-pixel-agents/blob/main/LICENSE)

</div>

<div align="center">
<a href="https://marketplace.visualstudio.com/items?itemName=rlarin.it-crowd-pixel-agents">🛒 VS Code Marketplace</a> •
<a href="https://plugins.jetbrains.com/plugin/27311-it-crowd-pixel-agents">🧠 JetBrains Marketplace</a> •
<a href="https://github.com/rlarin/it-crowd-pixel-agents/issues">🐛 Issues</a> •
<a href="CHANGELOG.md">📋 Changelog</a>
</div>

<br/>

> Based on [Pixel Agents](https://github.com/pixel-agents-hq/pixel-agents) by **Pablo De Lucca**, MIT licensed. This fork adds JetBrains IDE support and a standalone CLI server.

<br/>

Pixel Agents turns multi-agent AI systems into something you can actually see and manage. Each agent becomes a character in a pixel art office. They walk around, sit at their desk, and visually reflect what they are doing — typing when writing code, reading when searching files, waiting when it needs your attention.

Right now it works as a VS Code extension with Claude Code. The vision though, is a fully agent-agnostic, platform-agnostic interface for orchestrating any AI agents, deployable anywhere.

This is the source code for the free Pixel Agents extension for VS Code — install from the [VS Code Marketplace](https://marketplace.visualstudio.com/items?itemName=pablodelucca.pixel-agents) or [Open VSX](https://open-vsx.org/extension/pablodelucca/pixel-agents) with the full furniture catalog included.

![Pixel Agents screenshot](webview-ui/public/Screenshot.jpg)

## Features

- **One agent, one character** — every Claude Code terminal gets its own animated character
- **Live activity tracking** — characters animate based on what the agent is actually doing (writing, reading, running commands)
- **Office layout editor** — design your office with floors, walls, and furniture using a built-in editor
- **Speech bubbles** — visual indicators when an agent is waiting for input or needs permission
- **Sound notifications** — optional chime when an agent finishes its turn
- **Sub-agent visualization** — Task tool sub-agents spawn as separate characters linked to their parent
- **Persistent layouts** — your office design is saved and shared across VS Code windows
- **External asset directories** — load custom or third-party furniture packs from any folder on your machine
- **Diverse characters** — 6 diverse characters. These are based on the amazing work of [JIK-A-4, Metro City](https://jik-a-4.itch.io/metrocity-free-topdown-character-pack).

<p align="center">
  <img src="webview-ui/public/characters.png" alt="Pixel Agents characters" width="320" height="72" style="image-rendering: pixelated;">
</p>

## Requirements

- [Claude Code CLI](https://docs.anthropic.com/en/docs/claude-code) installed and configured
- **VS Code extension**: VS Code 1.105.0+
- **JetBrains plugin**: IntelliJ Platform 2025.1+, Node.js 18+
- **Platform**: Windows, Linux, and macOS

## Install

### VS Code extension

Search **"IT Crowd Pixel Agents"** in the VS Code Extensions panel, or:

```sh
code --install-extension rlarin.it-crowd-pixel-agents
```

### JetBrains plugin (PyCharm, WebStorm, IntelliJ…)

Install from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/27311-it-crowd-pixel-agents) or search **"IT Crowd Pixel Agents"** inside your IDE's plugin settings.

### npm / CLI

```sh
npm install -g it-crowd-pixel-agents
```

The CLI starts a standalone HTTP server and serves the Pixel Agents webview. It is used internally by the JetBrains plugin but can also be run directly:

```sh
it-crowd-pixel-agents [--host <host>] [--port <port>] [--project <dir>]
```

| Flag        | Default     | Description                                    |
| ----------- | ----------- | ---------------------------------------------- |
| `--host`    | `127.0.0.1` | HTTP server host                               |
| `--port`    | auto        | HTTP server port                               |
| `--project` | cwd         | Project directory to watch for Claude sessions |

## Getting Started

### VS Code

1. Open the **Pixel Agents** panel (bottom panel area, alongside your terminal)
2. Click **+ Agent** to spawn a Claude Code terminal — a character appears in the office
3. Watch your agents work

### From source

```bash
git clone https://github.com/rlarin/it-crowd-pixel-agents.git
cd it-crowd-pixel-agents
npm install && cd webview-ui && npm install && cd ..
npm run build
```

Then press **F5** in VS Code to launch the Extension Development Host.

### Usage

1. Open the **Pixel Agents** panel (bottom panel area, alongside your terminal)
2. Click **+ Agent** to spawn a Claude Code terminal — right-click for `--dangerously-skip-permissions`
3. Start coding with Claude — watch the character react in real time
4. Click a character to select it, then click a seat to reassign it
5. Click **Layout** to open the office editor and customize your space

## Layout Editor

The built-in editor lets you design your office:

- **Floor** — Full HSB color control
- **Walls** — Auto-tiling walls with color customization
- **Tools** — Select, paint, erase, place, eyedropper, pick
- **Undo/Redo** — 50 levels with Ctrl+Z / Ctrl+Y
- **Export/Import** — Share layouts as JSON files via the Settings modal

The grid is expandable up to 64×64 tiles. Click the ghost border outside the current grid to grow it.

### Office Assets

All office assets (furniture, floors, walls) are now **fully open-source** and included in this repository under `webview-ui/public/assets/`. No external purchases or imports are needed — everything works out of the box.

Each furniture item lives in its own folder under `assets/furniture/` with a `manifest.json` that declares its sprites, rotation groups, state groups (on/off), and animation frames. Floor tiles are individual PNGs in `assets/floors/`, and wall tile sets are in `assets/walls/`. This modular structure makes it easy to add, remove, or modify assets without touching any code.

To add a new furniture item, create a folder in `webview-ui/public/assets/furniture/` with your PNG sprite(s) and a `manifest.json`, then rebuild. The asset manager (`scripts/asset-manager.html`) provides a visual editor for creating and editing manifests.

To use furniture from an external directory, open Settings → **Add Asset Directory**. See [docs/external-assets.md](docs/external-assets.md) for the full manifest format and how to use third-party asset packs.

Characters are based on the amazing work of [JIK-A-4, Metro City](https://jik-a-4.itch.io/metrocity-free-topdown-character-pack).

## How It Works

Pixel Agents watches Claude Code's JSONL transcript files to track what each agent is doing. When an agent uses a tool (like writing a file or running a command), the extension detects it and updates the character's animation accordingly. No modifications to Claude Code are needed — it's purely observational.

The webview runs a lightweight game loop with canvas rendering, BFS pathfinding, and a character state machine (idle → walk → type/read). Everything is pixel-perfect at integer zoom levels.

## Tech Stack

- **Extension**: TypeScript, VS Code Webview API, esbuild
- **Webview**: React 19, TypeScript, Vite, Canvas 2D

## Known Limitations

- **Agent-terminal sync** — the way agents are connected to Claude Code terminal instances is not super robust and sometimes desyncs, especially when terminals are rapidly opened/closed or restored across sessions.
- **Heuristic-based status detection** — Claude Code's JSONL transcript format does not provide clear signals for when an agent is waiting for user input or when it has finished its turn. The current detection is based on heuristics (idle timers, turn-duration events) and often misfires — agents may briefly show the wrong status or miss transitions.
- **Linux/macOS tip** — if you launch VS Code without a folder open (e.g. bare `code` command), agents will start in your home directory. This is fully supported; just be aware your Claude sessions will be tracked under `~/.claude/projects/` using your home directory as the project root.

## Troubleshooting

If your agent appears stuck on idle or doesn't spawn:

1. **Debug View** — In the Pixel Agents panel, click the gear icon (Settings), then toggle **Debug View**. This shows connection diagnostics per agent: JSONL file status, lines parsed, last data timestamp, and file path. If you see "JSONL not found", the extension can't locate the session file.
2. **Debug Console** — If you're running from source (Extension Development Host via F5), open VS Code's **View > Debug Console**. Search for `[Pixel Agents]` to see detailed logs: project directory resolution, JSONL polling status, path encoding mismatches, and unrecognized JSONL record types.

## Where This Is Going

The long-term vision is an interface where managing AI agents feels like playing the Sims, but the results are real things built.

- **Agents as characters** you can see, assign, monitor, and redirect, each with visible roles (designer, coder, writer, reviewer), stats, context usage, and tools.
- **Desks as directories** — drag an agent to a desk to assign it to a project or working directory.
- **An office as a project** — with a Kanban board on the wall where idle agents can pick up tasks autonomously.
- **Deep inspection** — click any agent to see its model, branch, system prompt, and full work history. Interrupt it, chat with it, or redirect it.
- **Token health bars** — rate limits and context windows visualized as in-game stats.
- **Fully customizable** — upload your own character sprites, themes, and office assets. Eventually maybe even move beyond pixel art into 3D or VR.

For this to work, the architecture needs to be modular at every level:

- **Platform-agnostic**: VS Code extension today, Electron app, web app, or any other host environment tomorrow.
- **Agent-agnostic**: Claude Code today, but built to support Codex, OpenCode, Gemini, Cursor, Copilot, and others through composable adapters.
- **Theme-agnostic**: community-created assets, skins, and themes from any contributor.

We're actively working on the core module and adapter architecture that makes this possible. If you're interested to talk about this further, please visit our [Discussions Section](https://github.com/pixel-agents-hq/pixel-agents/discussions).

## Community & Contributing

Use **[Issues](https://github.com/rlarin/it-crowd-pixel-agents/issues)** to report bugs or request features.

See [CONTRIBUTING.md](CONTRIBUTING.md) for instructions on how to contribute.

Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before participating.

## Files written to disk

| Path                                   | Purpose                                        |
| -------------------------------------- | ---------------------------------------------- |
| `~/.pixel-agents/layout.json`          | Office layout (shared across all windows)      |
| `~/.pixel-agents/config.json`          | User config (external asset directories, etc.) |
| `~/.pixel-agents/hooks/claude-hook.js` | Hook listener script (copied on startup)       |
| `~/.pixel-agents/server.json`          | Running server port + auth token               |
| `~/.pixel-agents/server.log`           | Background server log                          |

## License

This project is licensed under the [MIT License](LICENSE).
Based on [Pixel Agents](https://github.com/pixel-agents-hq/pixel-agents) by Pablo De Lucca, MIT licensed.
