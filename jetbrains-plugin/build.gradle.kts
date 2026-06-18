import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.pixelagents"
version = "0.3.11"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
        bundledPlugin("org.jetbrains.plugins.terminal")
        testFramework(TestFrameworkType.Platform)
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testRuntimeOnly("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.7")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "io.github.rlarin.pixelagents"
        name = "IT Crowd Pixel Agents"
        version = "0.3.11"
        description = """
            JetBrains edition of Pixel Agents — a pixel art office where AI agents (Claude Code terminals)
            are animated characters you can watch work in real time.

            Compatible with PyCharm, WebStorm, and all JetBrains IDEs based on IntelliJ Platform 2025.1+.
            Requires Node.js to be installed.

            Based on Pixel Agents by Pablo De Lucca (https://github.com/pixel-agents-hq/pixel-agents), MIT licensed.
        """.trimIndent()
        changeNotes = """
            <h3>0.3.11</h3>
            <ul>
                <li><b>Fix: restored agents now appear immediately on load</b> — previously idle agents were hidden on startup and only revealed once they became active, so you had to click "Refresh agents" to see them. Restored agents (already filtered to live terminals) are now visible right away; the 5-minute idle-hide still applies to agents that go idle during the session.</li>
            </ul>
            <h3>0.3.10</h3>
            <ul>
                <li><b>Fix: idle agents now correctly leave after 5 minutes</b> — on reconnect the server was sending the layout before the agent list, so restored agents were never added to the canvas and the idle-hide timer never ran. Fixed message ordering so agents are buffered correctly before the layout flush.</li>
                <li><b>Agent limit enforced by work stations</b> — the "+ Agent" button is now disabled when all PC-facing seats are occupied. Hovering shows the current count (e.g. "All work stations are occupied (3/3)").</li>
            </ul>
            <h3>0.3.9</h3>
            <ul>
                <li><b>Only active agents appear on startup</b> — when the panel opens, idle agents are no longer shown. Restored agents start hidden and appear the moment Claude gives them a task; agents that go idle while you watch stay visible.</li>
                <li><b>Idle agents leave after 5 minutes</b> — lowered the long-idle timeout from 4 hours to 5 minutes. An idle agent leaves the office after 5 minutes and returns instantly when it picks up new work.</li>
            </ul>
            <h3>0.3.8</h3>
            <ul>
                <li><b>Idle agents start in the rest area</b> — on startup, restored agents that aren't actively working now appear seated at a non-work seat (couch, lounge chair) instead of at their desk, and lounge there until Claude picks up a new task.</li>
            </ul>
            <h3>0.3.7</h3>
            <ul>
                <li><b>Idle agents walk to the rest area</b> — when an agent finishes a turn it now walks to a non-work seat (couch, lounge chair — any chair not facing a screen) if one is free, instead of wandering aimlessly around the office.</li>
                <li><b>Long-idle agents disappear</b> — agents that have been inactive for 4 hours are hidden from the canvas. They reappear immediately the moment Claude picks up a new task.</li>
                <li><b>Fix: "Cannot find module …claude-hook.js"</b> — the hook script is now always copied to <code>~/.pixel-agents/hooks/</code> on startup, so the path registered in <code>~/.claude/settings.json</code> is never stale even after a reinstall or a hooks toggle.</li>
            </ul>
            <h3>0.3.6</h3>
            <ul>
                <li><b>Fix: "Node.js not found" on macOS even when Node is installed</b> — a GUI-launched IDE inherits only a minimal PATH that excludes Homebrew, nvm, fnm and volta. The background server is now launched through your login shell, so it sees Node exactly as your terminal does.</li>
            </ul>
            <h3>0.3.5</h3>
            <ul>
                <li><b>Fix: tool window froze on "Loading…" after a while</b> — the background server's output is now written to a log file instead of an undrained pipe. Previously the pipe's buffer could fill and block the server's event loop, leaving it alive but unresponsive. Server logs are now at <code>~/.pixel-agents/server.log</code>.</li>
            </ul>
            <h3>0.3.4</h3>
            <ul>
                <li><b>Fix: server failed to start in some projects</b> — the background server is now launched from a neutral directory, so it no longer fails when the open project happens to share a name with an npm package. The real project is still scanned correctly.</li>
                <li><b>Clear startup errors</b> — if the background server can't start (e.g. Node.js not installed), the panel now shows a helpful message and a Retry instead of an endless "Loading…".</li>
            </ul>
            <h3>0.3.3</h3>
            <ul>
                <li><b>Fix: stuck on "Loading…"</b> — the tool window no longer hangs when a stale or hung background server is left behind by a previous run. The server is now health-checked before being reused, and a fresh one is started (on a free port) when needed.</li>
            </ul>
            <h3>0.3.2</h3>
            <ul>
                <li><b>Fix: Export Layout now works</b> — previously clicking Export Layout in Settings did nothing in JetBrains. It now opens a native save dialog and writes the current layout to a JSON file.</li>
                <li><b>Fix: Import Layout now works</b> — previously clicking Import Layout in Settings did nothing in JetBrains. It now opens a native file picker, validates the selected layout, and applies it immediately.</li>
                <li><b>Fix: Open Sessions Folder now works</b> — it now reveals the Claude sessions folder for the current project in your OS file manager.</li>
            </ul>
            <h3>0.3.1</h3>
            <ul>
                <li><b>New app icon</b> — fresh pixel-art office illustration used as the extension icon.</li>
                <li><b>Custom toolbar icon</b> — dedicated SVG icon now appears in the JetBrains tool window tab instead of the generic window icon.</li>
            </ul>
            <h3>0.3.0</h3>
            <ul>
                <li>Tool window title set to "IT Crowd" with a real plugin icon.</li>
                <li>Manual refresh button for agents and office view.</li>
                <li>Widened <code>untilBuild</code> to 261.* for IntelliJ 2026.1 compatibility.</li>
            </ul>
            <h3>0.2.0</h3>
            <ul>
                <li>Initial JetBrains plugin release with embedded browser panel, Claude Code terminal integration, and standalone server.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "261.*"
        }
    }
    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
