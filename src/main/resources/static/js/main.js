'use strict';

var usernamePage = document.querySelector('#username-page');
var playPage = document.querySelector('#play-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var buttonArea = document.querySelector("#buttonArea");
var rockBotton = document.querySelector("#rock");
var paperButton = document.querySelector("#paper");
var scissorsButton = document.querySelector('#scissors');
var lostMessage = document.querySelector('.lost-message');
var winMessage = document.querySelector('.win-message');
var winFinalMessage = document.querySelector('.win-final-message');
var beginMessage = document.querySelector('.begin-message');
var drawMessage = document.querySelector('.draw-message');
var onlinePlayerElement = document.querySelector('#online-player');
var playerElement = document.querySelector('.player');

var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        playPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    stompClient.subscribe('/user/queue/msg',onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/play.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    connectingElement.classList.add('hidden');

    playerElement.innerText = 'Player: ' + username;
    playerElement.classList.remove('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendRock(event) {
    if(stompClient) {
        var playMessage = {
            sender: username,
            content: 'rock',
            type: 'GESTURE'
        };

        stompClient.send("/app/play.sendGesture", {}, JSON.stringify(playMessage));
    }
    event.preventDefault();
}

function sendPaper(event) {
    if(stompClient) {
        var playMessage = {
            sender: username,
            content: 'paper',
            type: 'GESTURE'
        };

        stompClient.send("/app/play.sendGesture", {}, JSON.stringify(playMessage));
    }
    event.preventDefault();
}

function sendScissors(event) {
    if(stompClient) {
        var playMessage = {
            sender: username,
            content: 'scissors',
            type: 'GESTURE'
        };

        stompClient.send("/app/play.sendGesture", {}, JSON.stringify(playMessage));
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else if (message.type === 'NOTICE') {
        messageElement.classList.add('event-message');
        if (message.content.indexOf('game begin') != -1) {
            beginMessage.classList.remove('hidden');
            setTimeout(function () {
                beginMessage.classList.add('hidden');
            }, 2000);

            buttonArea.classList.remove('hidden');
            if (!winMessage.classList.contains('hidden')) {
                winMessage.classList.add('hidden');
            }
        } else if (message.content.indexOf('draw') != -1) {
            drawMessage.classList.remove('hidden');
            setTimeout(function () {
                drawMessage.classList.add('hidden');
            }, 3000);
        }
    } else if (message.type === 'ONLINE') {
        onlinePlayerElement.innerText = message.content;
        return;
    } else if (message.type === 'CONTINUE') {
        if (message.content.indexOf('win') != -1) {
            message.content = '### you win, wait another player for 5s ###'
            winMessage.classList.remove('hidden');
            setTimeout(function () {
                winMessage.classList.add('hidden');
            }, 10000);
        }
        messageElement.classList.add('event-message');
    } else if (message.type === 'END') {
        if (message.content.indexOf('lost') != -1) {
            message.content = '### you lost ###'
            lostMessage.classList.remove('hidden');
            setTimeout(function () {
                lostMessage.classList.add('hidden');
            }, 3000);
        } else if (message.content.indexOf('win') != -1) {
            if (!winMessage.classList.contains('hidden')) {
                winMessage.classList.add('hidden');
            }
            message.content = '### you win finally ###'
            winFinalMessage.classList.remove('hidden');
            setTimeout(function () {
                winFinalMessage.classList.add('hidden');
            }, 10000);
        }

        messageElement.classList.add('event-message');
        buttonArea.classList.add("hidden");
    }
    else {
        messageElement.classList.addbootstrap.css('play-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
rockBotton.addEventListener('click', sendRock, true)
paperButton.addEventListener('click', sendPaper, true)
scissorsButton.addEventListener('click', sendScissors, true)