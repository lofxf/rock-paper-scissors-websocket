package com.websocket.Listener;

import com.websocket.Service.WebSocketService;
import com.websocket.controller.PlayerController;
import com.websocket.model.PlayerMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    WebSocketService webSocketService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            log.info("User Disconnected : " + username);
            PlayerMessage playerMessage = new PlayerMessage();
            playerMessage.setType(PlayerMessage.MessageType.LEAVE);
            playerMessage.setSender(username);
            webSocketService.sendMsg(playerMessage);
        }
        if (PlayerController.onlinePlayerList.contains(sessionId))
            PlayerController.onlinePlayerList.remove(sessionId);
    }

}
