const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { io: ClientIO } = require("socket.io-client");
const { Server } = require("socket.io");
const app = express();
const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: ["http://localhost:3000", "http://localhost:8000"],  // Allow requests from localhost:8000
        methods: ["GET", "POST"],
        allowedHeaders: ["my-custom-header"],
        credentials: true
    }
});
let currentText = ""
let lastHeartbeat_receive = null;

const otherServerUrl = 'http://192.168.1.68:3000'; // Change this URL to the actual other server's URL
const otherServerSocket = ClientIO(otherServerUrl);

const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('./mydatabase.db', (err) => {
    if (err) {
        console.error('Error opening database ' + err.message);
    } else {
        db.run('CREATE TABLE IF NOT EXISTS texts(id INTEGER PRIMARY KEY AUTOINCREMENT,text_content TEXT)', (err) => {
            if (err) {
                console.error("Creating table error: " + err.message);
            }
        });
    }
});

const PORT = 8000;
// Change localhost to 0.0.0.0 to allow connections from any device on the network
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

app.use(express.static('public')); // Serve static files from the 'public' directory

otherServerSocket.on('connect', () => {
    console.log('Connected to server A');
    lastHeartbeat = Date.now();
});


io.on('connection', (socket) => {
    console.log('A user connected');

    // Emit a heartbeat every few seconds
    setInterval(() => {
        otherServerSocket.emit('heartbeat', { time: Date.now() });
    }, 1000);
    
    
    // Send current text to the user upon connection
     db.get("SELECT text_content FROM texts WHERE id = ?", [1], (err, row) => {
        if (err) {
            console.error(err.message);
        } else {
            socket.emit('load-text', row ? row.text_content : "");
        }
    });

    // check if receive message from serverA
    socket.on('forward-text-change', (text) => {
        io.emit('text-update', text);
    });


    // check if received heartbeat from serverA
    socket.on('heartbeat',(data) => {
        lastHeartbeat_receive = Date.now();
    })

    // Receive text update from any client
    // socket.on('send-text', (text) => { 
    //     socket.broadcast.emit('receive-text', text);  // Confirm this is being called
    // });

    socket.on('text-change', (text) => {
        currentText = text;
        // Broadcast changes to all other clients
        socket.broadcast.emit('text-update', text);
        // Send changes to other server
        otherServerSocket.emit('forward-text-change', text);
        // Update the database
        db.run(`UPDATE texts SET text_content = ? WHERE id = ?`, [text, 1], function(err) {
            if (err) {
                console.error("Database update error: " + err.message);
            } else {
                console.log("Text updated in database");
            }
        });
    });

    socket.on('disconnect', () => {
        db.run(`INSERT INTO texts (text_content) VALUES (?)`, [currentText], function(err) {
            if (err) {
                console.error("Error inserting text into database: " + err.message);
            } else {
                console.log("Text saved on disconnect");
            }
        });
    });

    // check every 2s if the other server is down
    setInterval(() => {
        if (Date.now() - lastHeartbeat_receive > 5000 || lastHeartbeat_receive == null) { 
            console.error('Other server is down!');
            // Additional logic to handle server down
        }else{
        lastHeartbeat = lastHeartbeat_receive;
        console.log('other server is working');}
    }, 5000);
});

