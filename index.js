const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(express.json());
app.use(cors());

let itemList = ['Item 1', 'Item 2', 'Item 3'];

// REST API for adding an item to the list
app.post('/add', (req, res) => {
    const newItem = req.body.item;
    if (newItem) {
        itemList.push(newItem);
        io.emit('item_added', newItem);
        res.status(200).send({ list: itemList });
    } else {
        res.status(400).send({ error: 'Invalid item' });
    }
});

// REST API for deleting an item from the list by index
app.delete('/delete/:index', (req, res) => {
    const index = parseInt(req.params.index, 10);
    if (index >= 0 && index < itemList.length) {
        itemList.splice(index, 1);
        io.emit('item_deleted', index);
        res.status(200).send({ list: itemList });
    } else {
        res.status(400).send({ error: 'Invalid index' });
    }
});

io.on('connection', (socket) => {
    console.log('A user connected');
    socket.emit('initial_list', itemList);

    socket.on('disconnect', () => {
        console.log('User disconnected');
    });
});

const PORT = 3000;
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});