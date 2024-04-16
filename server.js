<<<<<<< HEAD
const express = require('express');
const http = require('http');
const app = express();
const server = http.createServer(app);
const io = require('socket.io')(server);
const redisAdapter = require('socket.io-redis');
io.adapter(redisAdapter({ host: 'localhost', port: 4000 }));

io.on('connection', function(socket) {
    socket.on('text change', function(msg) {
        socket.broadcast.emit('text update', msg);
    });
});

const PORT = 4000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
=======
const express = require('express');
const http = require('http');
const app = express();
const server = http.createServer(app);
const io = require('socket.io')(server);
const redisAdapter = require('socket.io-redis');
io.adapter(redisAdapter({ host: 'localhost', port: 4000 }));

io.on('connection', function(socket) {
    socket.on('text change', function(msg) {
        socket.broadcast.emit('text update', msg);
    });
});

const PORT = 4000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
>>>>>>> d0ed56b (update heartbeat feature april 15)
});