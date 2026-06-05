// Connects to the running Pixel Agents server WS, sends webviewReady, and
// reports the asset messages received (types + sizes + sprite counts).
import fs from 'fs';
import os from 'os';
import path from 'path';
import WebSocket from 'ws';

const cfg = JSON.parse(
  fs.readFileSync(path.join(os.homedir(), '.pixel-agents', 'server.json'), 'utf-8'),
);
console.log('server.json port:', cfg.port);

const ws = new WebSocket(`ws://localhost:${cfg.port}/ws`, {
  headers: { Authorization: `Bearer ${cfg.token}` },
});

const seen = {};
ws.on('open', () => {
  console.log('WS open; sending webviewReady');
  ws.send(JSON.stringify({ type: 'webviewReady' }));
});
ws.on('message', (data) => {
  const size = data.length ?? data.byteLength ?? 0;
  let msg;
  try {
    msg = JSON.parse(data.toString());
  } catch {
    console.log('non-JSON message, size', size);
    return;
  }
  seen[msg.type] = (seen[msg.type] || 0) + 1;
  let extra = '';
  if (msg.type === 'floorTilesLoaded') extra = ` sprites=${msg.sprites?.length}`;
  if (msg.type === 'wallTilesLoaded') extra = ` sets=${msg.sets?.length}`;
  if (msg.type === 'characterSpritesLoaded') extra = ` chars=${msg.characters?.length}`;
  if (msg.type === 'furnitureAssetsLoaded')
    extra = ` catalog=${msg.catalog?.length} sprites=${Object.keys(msg.sprites ?? {}).length}`;
  if (msg.type === 'layoutLoaded')
    extra = ` cols=${msg.layout?.cols} rows=${msg.layout?.rows} tiles=${msg.layout?.tiles?.length} tileSample=${JSON.stringify(msg.layout?.tiles?.slice(0, 12))}`;
  console.log(`<- ${msg.type} (${size} bytes)${extra}`);
});
ws.on('error', (e) => console.log('WS error:', e.message));
setTimeout(() => {
  console.log('--- summary ---', JSON.stringify(seen));
  ws.close();
  process.exit(0);
}, 6000);
