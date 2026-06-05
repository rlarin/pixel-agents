/**
 * Redirect all console output to a rotating-ish logfile instead of stdout/stderr.
 *
 * WHY: the standalone server is launched by a host process (the JetBrains plugin's
 * ServerManager) that does NOT drain the child's stdout/stderr. Node writes to a
 * pipe *synchronously*; once the OS pipe buffer (~64KB on Windows) fills, every
 * `console.log` blocks, which freezes the event loop. The HTTP server then stops
 * answering (alive PID, dead /api/health) and the embedded browser hangs on
 * "Loading..." forever.
 *
 * Writing to a file stream instead keeps the inherited stdout/stderr pipe empty,
 * so the server can log freely without ever deadlocking — independent of whether
 * the host drains the pipe. Importing this module for its side effect MUST happen
 * before any logging occurs, so it is the first import in cli.ts.
 */

import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

function setup(): void {
  // Opt-out: PIXEL_AGENTS_LOG_STDOUT=1 keeps logs on the console (dev/debug).
  if (process.env.PIXEL_AGENTS_LOG_STDOUT === '1') return;

  try {
    const dir = path.join(os.homedir(), '.pixel-agents');
    fs.mkdirSync(dir, { recursive: true });
    const logPath = path.join(dir, 'server.log');

    // Truncate on each fresh start so the file does not grow unbounded across runs.
    const stream = fs.createWriteStream(logPath, { flags: 'w' });

    const write = (level: string, args: unknown[]): void => {
      const line = args
        .map((a) =>
          typeof a === 'string'
            ? a
            : a instanceof Error
              ? (a.stack ?? a.message)
              : safeStringify(a),
        )
        .join(' ');
      stream.write(`${new Date().toISOString()} [${level}] ${line}\n`);
    };

    console.log = (...args: unknown[]) => write('log', args);
    console.info = (...args: unknown[]) => write('info', args);
    console.warn = (...args: unknown[]) => write('warn', args);
    console.error = (...args: unknown[]) => write('error', args);
    console.debug = (...args: unknown[]) => write('debug', args);
  } catch {
    // If file logging can't be set up, fall back to the default console (rare).
  }
}

function safeStringify(value: unknown): string {
  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
}

setup();
