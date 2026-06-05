package com.pixelagents

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.io.File
import java.util.UUID
import javax.swing.JComponent
import javax.swing.JLabel

class PixelAgentsBrowserPanel(
    private val project: Project,
    private val portFuture: java.util.concurrent.CompletableFuture<Int>,
    parentDisposable: Disposable,
) : Disposable {

    private val browser: JBCefBrowser?
    private val launchAgentQuery: JBCefJSQuery?
    private val nativeQuery: JBCefJSQuery?
    private val terminalManager = TerminalManager(project)
    private val fallbackComponent: JComponent?
    private val loadHandler: CefLoadHandlerAdapter?

    init {
        if (JBCefApp.isSupported()) {
            val b = JBCefBrowser()
            browser = b
            val query = JBCefJSQuery.create(b as JBCefBrowserBase)
            launchAgentQuery = query

            query.addHandler { _ ->
                val sessionId = UUID.randomUUID().toString()
                terminalManager.openTerminal(sessionId)
                null
            }

            val native = JBCefJSQuery.create(b)
            nativeQuery = native
            native.addHandler { request ->
                when (request) {
                    "openSessionsFolder" -> {
                        revealSessionsFolder()
                        null
                    }
                    "exportLayout" -> {
                        exportLayout()
                        null
                    }
                    "importLayout" -> JBCefJSQuery.Response(importLayoutPickAndRead())
                    else -> null
                }
            }

            val handler = object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(cefBrowser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                    if (frame.isMain) injectBridge()
                }
            }
            loadHandler = handler
            b.jbCefClient.addLoadHandler(handler, b.cefBrowser)

            portFuture.thenAccept { port ->
                browser?.loadURL("http://localhost:$port?ide=jetbrains")
            }.exceptionally { _ ->
                // Server failed to start (commonly: Node.js missing). Show a clear
                // message instead of leaving the panel blank / stuck "Loading…".
                browser?.loadHTML(SERVER_ERROR_HTML)
                null
            }

            fallbackComponent = null
            Disposer.register(parentDisposable, this)
        } else {
            browser = null
            launchAgentQuery = null
            nativeQuery = null
            loadHandler = null
            fallbackComponent = JLabel(
                "<html><body style='padding:16px'>" +
                    "<b>Pixel Agents</b><br><br>" +
                    "JCEF is not available in this IDE configuration.<br>" +
                    "Please use the bundled JetBrains Runtime to enable the embedded browser." +
                    "</body></html>"
            )
        }
    }

    private fun injectBridge() {
        val b = browser ?: return
        val query = launchAgentQuery ?: return
        val native = nativeQuery ?: return
        val js = """
            window.pixelAgentsBridge = {
                launchAgent: function(folderPath, bypassPermissions) {
                    ${query.inject("JSON.stringify({folderPath: folderPath, bypassPermissions: bypassPermissions})")}
                },
                openSessionsFolder: function() {
                    ${native.inject("'openSessionsFolder'")}
                },
                exportLayout: function() {
                    ${native.inject("'exportLayout'")}
                },
                importLayout: function(cb) {
                    ${native.inject("'importLayout'", "cb", "function(errCode, errMsg) {}")}
                }
            };
        """.trimIndent()
        b.cefBrowser.executeJavaScript(js, b.cefBrowser.url, 0)
    }

    // ── Native settings actions ──────────────────────────────────

    /** Claude stores sessions at ~/.claude/projects/<path-with-non-alnum-as-dashes>/. */
    private fun sessionsDir(): File? {
        val basePath = project.basePath ?: return null
        val dirName = basePath.replace(Regex("[^a-zA-Z0-9-]"), "-")
        val home = System.getProperty("user.home")
        val exact = File(home, ".claude/projects/$dirName")
        if (exact.isDirectory) return exact
        // Case-insensitive fallback (Windows drive-letter casing differs from Claude's).
        val projectsRoot = File(home, ".claude/projects")
        return projectsRoot.listFiles()?.firstOrNull { it.name.equals(dirName, ignoreCase = true) }
            ?: if (projectsRoot.isDirectory) projectsRoot else null
    }

    private fun layoutFile(): File = File(System.getProperty("user.home"), ".pixel-agents/layout.json")

    private fun revealSessionsFolder() {
        val dir = sessionsDir() ?: return
        ApplicationManager.getApplication().invokeLater {
            RevealFileAction.openDirectory(dir)
        }
    }

    private fun exportLayout() {
        val source = layoutFile()
        if (!source.isFile) return
        val content = source.readText()
        ApplicationManager.getApplication().invokeLater {
            val descriptor = FileSaverDescriptor("Export Layout", "Save the office layout as a JSON file", "json")
            val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val wrapper = dialog.save(null as java.nio.file.Path?, "pixel-agents-layout.json") ?: return@invokeLater
            wrapper.file.writeText(content)
        }
    }

    /** Opens a file picker, validates the chosen layout, returns its JSON text (or "" on cancel/invalid). */
    private fun importLayoutPickAndRead(): String {
        val result = arrayOf("")
        ApplicationManager.getApplication().invokeAndWait {
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter { it.extension.equals("json", ignoreCase = true) }
                .withTitle("Import Layout")
            val file = FileChooser.chooseFile(descriptor, project, null) ?: return@invokeAndWait
            val text = String(file.contentsToByteArray(), Charsets.UTF_8)
            if (text.contains("\"version\"") && text.contains("\"tiles\"")) {
                result[0] = text
            }
        }
        return result[0]
    }

    val component: JComponent
        get() = browser?.component ?: fallbackComponent!!

    /** Hard reload: re-navigates the embedded browser, re-initializing the webview. */
    fun reload() {
        val b = browser ?: return
        if (portFuture.isDone && !portFuture.isCompletedExceptionally) {
            b.loadURL("http://localhost:${portFuture.get()}?ide=jetbrains")
        } else {
            b.cefBrowser.reload()
        }
    }

    override fun dispose() {
        val b = browser
        if (b != null && loadHandler != null) {
            b.jbCefClient?.removeLoadHandler(loadHandler, b.cefBrowser)
        }
        launchAgentQuery?.dispose()
        nativeQuery?.dispose()
        browser?.dispose()
    }

    companion object {
        private val SERVER_ERROR_HTML = """
            <html><body style="font-family: sans-serif; background:#1e1e2e; color:#cdd6f4; padding:24px">
                <h3>Couldn't start the Pixel Agents server</h3>
                <p>The background server (launched via <code>npx</code>) didn't come up.</p>
                <p>Make sure <b>Node.js</b> is installed and on your PATH, then reopen the tool window
                   (or use the refresh button).</p>
                <p style="opacity:.6">Check it from a terminal: <code>node --version</code></p>
            </body></html>
        """.trimIndent()
    }
}
