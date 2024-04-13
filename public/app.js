const socket = io();  // Connect to the server

const editor = document.getElementById('editor');
console.log(editor);  // Check what this outputs
if (editor) {
    editor.addEventListener('input', function() {
        // Your code here
    });
} else {
    console.error('Editor element not found!');
}


document.getElementById('editor').addEventListener('input', function() {
    const text = this.value;
    console.log('Emitting text:', text);
    socket.emit('send-text', text);
});

socket.on('receive-text', (text) => {
    const editor = document.getElementById('editor');
    // Avoid overwriting if the user is currently typing
    if (document.activeElement !== editor) {
        editor.value = text;
    }
});

socket.on('connect', () => {
    console.log('Connected to server');
});


