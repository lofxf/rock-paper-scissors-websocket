package com.websocket.controller;

import com.websocket.RockPaperScissorsWebsocketApplication;
import com.websocket.Service.WebSocketService;
import com.websocket.model.Player;
import com.websocket.model.PlayerMessage;
import com.websocket.model.PlayerStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Controller
public class PlayerController {

    @Autowired
    WebSocketService webSocketService;

    public static boolean refereeFlag = false;

    public final static Queue<Player> playerQueue = new LinkedBlockingQueue<>();
    public final static List<Player> nowPlayer = new ArrayList<>();
    public final static List<String> onlinePlayerList = new ArrayList<>();
    @MessageMapping("/play.sendGesture")
//    @SendTo("/topic/public")
    public PlayerMessage sendMessage(@Payload PlayerMessage playerMessage,
                                     SimpMessageHeaderAccessor headerAccessor) {
        for (int i = 0; i < nowPlayer.size(); i ++) {
            Player player = nowPlayer.get(i);
            String sessionId = nowPlayer.get(i).getSessionId();
            if (headerAccessor.getSessionId() == sessionId) {
                player.setGesture(playerMessage.getContent());
            }
        }
        return playerMessage;
    }

    @MessageMapping("/play.addUser")
    @SendTo("/topic/public")
//    @SendToUser("/queue/msg")
    public PlayerMessage addUser(@Payload PlayerMessage playerMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        String name = playerMessage.getSender();
        // Add name in web socket session
        headerAccessor.getSessionAttributes().put("name", playerMessage.getSender());
        String sessionId = headerAccessor.getSessionId();
        Player player = new Player();
        player.setSessionId(sessionId);
        player.setName(name);
        player.setStatus("first");
        playerQueue.add(player);
        onlinePlayerList.add(sessionId);
        return playerMessage;
    }
}
