// WebSocket -> TCP bridge
// Each WebSocket connection opens a TCP socket to localhost:5555 and forwards data both ways.
// Usage: node ws-bridge.js [wsPort] [tcpHost] [tcpPort]

const WebSocket = require('ws');
const net = require('net');

const WS_PORT = process.argv[2] ? parseInt(process.argv[2], 10) : 8080;
const TCP_HOST = process.argv[3] || '127.0.0.1';
const TCP_PORT = process.argv[4] ? parseInt(process.argv[4], 10) : 5555;

const wss = new WebSocket.Server({ port: WS_PORT });

console.log(`WS->TCP bridge starting: ws://localhost:${WS_PORT} -> ${TCP_HOST}:${TCP_PORT}`);

wss.on('connection', (ws, req) => {
  console.log('WS client connected:', req.socket.remoteAddress, req.socket.remotePort);

  const socket = new net.Socket();
  socket.setEncoding('utf8');

  socket.connect(TCP_PORT, TCP_HOST, () => {
    console.log('Connected to TCP server', TCP_HOST + ':' + TCP_PORT);
  });

  // WS -> TCP
  ws.on('message', (msg) => {
    try {
      // assume text messages; append newline to emulate telnet/line protocol
      socket.write(msg.toString() + '\n');
    } catch (ex) {
      console.error('Error writing to TCP socket', ex);
    }
  });

  ws.on('close', () => {
    console.log('WS closed, destroying TCP socket');
    try { socket.end(); socket.destroy(); } catch (e) {}
  });

  ws.on('error', (err) => {
    console.log('WS error', err);
    try { socket.end(); socket.destroy(); } catch (e) {}
  });

  // TCP -> WS
  socket.on('data', (data) => {
    if (ws.readyState === WebSocket.OPEN) {
      // send raw text data to websocket client
      ws.send(data.toString());
    }
  });

  socket.on('close', () => {
    console.log('TCP socket closed');
    try { ws.close(); } catch (e) {}
  });

  socket.on('error', (err) => {
    console.error('TCP socket error', err);
    try { ws.close(); } catch (e) {}
  });
});

wss.on('listening', () => console.log('WebSocket server listening on port', WS_PORT));
