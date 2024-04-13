const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(express.static('public')); // Serve static files from the 'public' directory

io.on('connection', (socket) => {
    console.log('A user connected');

    // Receive text update from any client
    socket.on('send-text', (text) => {
        console.log(`Received text from ${socket.id}: ${text}`);
        socket.broadcast.emit('receive-text', text);  // Confirm this is being called
        console.log(`Broadcasted text from ${socket.id}`);
    });

    socket.on('disconnect', () => {
        console.log('User disconnected');
    });
});

const PORT = 3000;
// Change localhost to 0.0.0.0 to allow connections from any device on the network
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
