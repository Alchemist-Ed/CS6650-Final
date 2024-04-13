const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const { io: ClientIO } = require("socket.io-client");

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const otherServerUrl = 'http://192.168.1.68:8000'; // Change this URL to the actual other server's URL
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

app.use(express.static('public')); // Serve static files from the 'public' directory

otherServerSocket.on('connect', () => {
    console.log('Connected to other server as client.');
});

io.on('connection', (socket) => {
    console.log('A user connected');

    // Send current text to the user upon connection
    db.get("SELECT text_content FROM texts WHERE id = ?", [1], (err, row) => {
        if (err) {
            console.error(err.message);
        } else {
            socket.emit('load-text', row ? row.text_content : "");
        }
    });
    
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


    otherServerSocket.on('forward-text-change', (text) => {
        // Broadcast these changes to local clients
        console.log(`Received forwarded text from other server: ${text}`);
        io.emit('text-update', text);
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


    
});

const PORT = 3000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});


