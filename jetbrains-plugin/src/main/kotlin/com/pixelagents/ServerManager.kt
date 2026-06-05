package com.pixelagents

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

@Serializable
private data class ServerConfig(
    val port: Int,
    val pid: Long,
    val token: String,
    val startedAt: Long,
)

class ServerManager(
    private val projectDir: String,
    private val serverJsonPath: Path = Paths.get(
        System.getProperty("user.home"), ".pixel-agents", "server.json"
    ),
    private val processAliveChecker: (Long) -> Boolean = { pid ->
        ProcessHandle.of(pid).map { it.isAlive }.orElse(false)
    },
    private val processLauncher: (String) -> Process = { dir ->
        val cmd = if (System.getProperty("os.name").startsWith("Windows"))
            listOf("cmd", "/c", "npx", "-y", "it-crowd-pixel-agents")
        else
            listOf("npx", "-y", "it-crowd-pixel-agents")
        // Run npx from a neutral directory (user home), NOT the project dir:
        // if the open project itself is the "it-crowd-pixel-agents" package
        // (e.g. its own repo), `npm exec` resolves the local package and fails
        // with "is not recognized", so the server never starts. The real
        // project is passed to the server via PIXEL_AGENTS_PROJECT_DIR instead.
        //
        // Redirect the child's stdout+stderr to a logfile (NOT an inherited
        // pipe). We never drain the child's output, so an undrained pipe would
        // fill its ~64KB OS buffer and block Node's synchronous stdout writes,
        // freezing the server event loop (alive PID, dead HTTP) — the classic
        // "stuck on Loading..." failure. A file sink can never back-pressure.
        val logFile = File(System.getProperty("user.home"), ".pixel-agents/server.log")
        logFile.parentFile?.mkdirs()
        val pb = ProcessBuilder(cmd)
            .directory(File(System.getProperty("user.home")))
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.to(logFile))
        pb.environment()["PIXEL_AGENTS_PROJECT_DIR"] = dir
        pb.start()
    },
    private val healthChecker: (Int) -> Boolean = { port ->
        try {
            val conn = URL("http://localhost:$port/api/health").openConnection() as HttpURLConnection
            conn.connectTimeout = 1000
            conn.readTimeout = 1000
            conn.responseCode == 200
        } catch (_: Exception) { false }
    },
    private val maxRetries: Int = 30,
    private val executor: (Runnable) -> Unit = { r ->
        ApplicationManager.getApplication().executeOnPooledThread(r)
    },
) : Disposable {

    private val log = Logger.getInstance(ServerManager::class.java)
    @Volatile private var ownedProcess: Process? = null
    private var config: ServerConfig? = null

    val portFuture: CompletableFuture<Int> = CompletableFuture()

    val serverPort: Int get() = portFuture.getNow(DEFAULT_PORT)

    fun start() {
        // Health-check off the EDT: a hung server (alive PID, dead HTTP) or a
        // stale server.json from a crash would otherwise be trusted blindly,
        // leaving the embedded browser stuck on "Loading..." forever.
        executor(Runnable {
            val existing = readServerJson()
            if (existing != null && processAliveChecker(existing.pid) && healthChecker(existing.port)) {
                config = existing
                log.info("Reusing healthy Pixel Agents server on port ${existing.port}")
                portFuture.complete(existing.port)
                return@Runnable
            }
            if (existing != null) {
                log.info(
                    "Ignoring stale/unhealthy server.json (PID ${existing.pid}, port ${existing.port}); launching fresh server"
                )
            }
            launchServer()
        })
    }

    private fun readServerJson(): ServerConfig? {
        val file = serverJsonPath.toFile()
        if (!file.exists()) return null
        return try {
            Json.decodeFromString<ServerConfig>(file.readText())
        } catch (_: Exception) { null }
    }

    private fun launchServer() {
        log.info("Starting Pixel Agents server...")
        val process = processLauncher(projectDir)
        ownedProcess = process

        repeat(maxRetries) {
            Thread.sleep(500)
            val cfg = readServerJson()
            if (cfg != null && healthChecker(cfg.port)) {
                config = cfg
                log.info("Pixel Agents server ready on port ${cfg.port}")
                portFuture.complete(cfg.port)
                return
            }
        }
        log.warn("Pixel Agents server did not become healthy within 15s")
        ownedProcess?.destroy()
        ownedProcess = null
        portFuture.completeExceptionally(RuntimeException("Pixel Agents server failed to start"))
    }

    override fun dispose() {
        ownedProcess?.let {
            if (it.isAlive) {
                it.destroy()
                log.info("Pixel Agents server stopped")
            }
        }
    }

    companion object {
        const val DEFAULT_PORT = 3100
    }
}
