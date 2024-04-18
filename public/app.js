const serverList = ['http://192.168.1.68:3000','http://192.168.1.68:8000']
socket = io();  // Connect to the server

document.getElementById('editor').addEventListener('input', function() {
    const text = this.value;
    console.log('Emitting text:', text);
    socket.emit('send-text', text);
    socket.emit('text-change', text);  // Emit text updates including deletions
});

socket.on('load-text', (text) => {
    document.getElementById('editor').value = text;
});

socket.on('text-update', (text) => {
    console.log(`Update received: ${text}`);
    document.getElementById('editor').value = text;
});


socket.on('receive-text', (text) => {
    const editor = document.getElementById('editor');
    // Avoid overwriting if the user is currently typing
    if (document.activeElement !== editor) {
        editor.value = text;
    }
});


socket.on('connect_error', (error)=>{
    console.error(`Connection to ${server} failed:`, error);
    if(server == serverList[0]){
        server = serverList[1];
        socket = io(server)
    }else{
        server = serverList[0];
        socket = io(server)
    }
});

socket.on('text-change', (text) => {
    currentText = text;
    // Broadcast changes to all local clients
    io.emit('text-update', text);
    // Update the database
    db.run(`UPDATE texts SET text_content = ? WHERE id = ?`, [text, 1], function(err) {
        if (err) {
            console.error("Database update error: " + err.message);
        } else {
            console.log("Text updated in database");
        }
    });
});
