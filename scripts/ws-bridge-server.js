// ws-bridge-server.js
// Serves static files (ws-test.html) and accepts WebSocket connections on the same port.
// For each WS connection, opens a TCP socket to the game server and proxies lines bidirectionally.
// Usage: node ws-bridge-server.js [port] [tcpHost] [tcpPort]

const http = require('http');
const path = require('path');
const fs = require('fs');
const net = require('net');
const WebSocket = require('ws');

const PORT = process.argv[2] ? parseInt(process.argv[2], 10) : 8080;
const TCP_HOST = process.argv[3] || '127.0.0.1';
const TCP_PORT = process.argv[4] ? parseInt(process.argv[4], 10) : 5555;

const publicDir = __dirname; // serve files from scripts/ (ws-test.html)

const server = http.createServer((req, res) => {
  // serve ws-test.html at '/'
  let reqPath = req.url.split('?')[0];
  if (reqPath === '/') reqPath = '/ws-test.html';
  const filePath = path.join(publicDir, reqPath.replace(/^[\/]+/, ''));
  fs.stat(filePath, (err, st) => {
    if (err || !st.isFile()) {
      res.writeHead(404, {'Content-Type':'text/plain'});
      res.end('Not found');
      return;
    }
    const stream = fs.createReadStream(filePath);
    const ext = path.extname(filePath).toLowerCase();
    const contentType = ext === '.html' ? 'text/html; charset=utf-8' : (ext === '.js' ? 'application/javascript' : 'application/octet-stream');
    res.writeHead(200, {'Content-Type': contentType});
    stream.pipe(res);
  });
});

const wss = new WebSocket.Server({ noServer: true });

server.on('upgrade', (request, socket, head) => {
  // Accept all upgrades as WebSocket for this demo
  wss.handleUpgrade(request, socket, head, (ws) => {
    wss.emit('connection', ws, request);
  });
});

wss.on('connection', (ws, req) => {
  console.log('WS client connected:', req.socket.remoteAddress, req.socket.remotePort);
  const socket = new net.Socket();
  socket.setEncoding('utf8');
  socket.connect(TCP_PORT, TCP_HOST, () => {
    console.log('Connected to TCP server', TCP_HOST + ':' + TCP_PORT);
  });

  ws.on('message', (msg) => {
    try { socket.write(msg.toString() + '\n'); } catch (ex) { console.error('Error writing to TCP socket', ex); }
  });

  ws.on('close', () => { console.log('WS closed, destroying TCP socket'); try { socket.end(); socket.destroy(); } catch (e) {} });
  ws.on('error', (err) => { console.log('WS error', err); try { socket.end(); socket.destroy(); } catch (e) {} });

  socket.on('data', (data) => { if (ws.readyState === WebSocket.OPEN) ws.send(data.toString()); });
  socket.on('close', () => { console.log('TCP socket closed'); try { ws.close(); } catch (e) {} });
  socket.on('error', (err) => { console.error('TCP socket error', err); try { ws.close(); } catch (e) {} });
});

server.listen(PORT, () => {
  console.log(`HTTP+WS bridge listening on http://localhost:${PORT} -> TCP ${TCP_HOST}:${TCP_PORT}`);
});
