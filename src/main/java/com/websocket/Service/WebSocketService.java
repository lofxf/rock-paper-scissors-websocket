package com.websocket.Service;

import com.websocket.model.PlayerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendMsg(PlayerMessage msg) {
        simpMessagingTemplate.convertAndSend("/topic/public", msg);
    }

    public void sendToClient(String sessionId, PlayerMessage msg) {
        simpMessagingTemplate.convertAndSendToUser(sessionId,"/queue/msg", msg,
                createHeaders(sessionId));
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
