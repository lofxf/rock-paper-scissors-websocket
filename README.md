# It's a rock–paper–scissors game based on WebSocket.

## Requirements

1. Docker
2. docker-compose

## Steps to Setup

1. Clone the application
```
git clone https://github.com/lofxf/rock-paper-scissors-websocket.git
```
2. Build and start application
```
# enter the root path of rock-paper-scissors-websocket
sudo docker-compose up
```
3. Check the application at http://127.0.0.1:8080/

## Game Rules

1. Open http://127.0.0.1:8080/ and enter a name, then you will join the game.
2. When there are more than 2 players the game will begin.
3. The computer play a role of referee, so there is no need extra referee.
