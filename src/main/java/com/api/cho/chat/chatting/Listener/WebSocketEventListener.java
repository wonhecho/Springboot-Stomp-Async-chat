package com.api.cho.chat.chatting.Listener;

import com.api.cho.chat.chatting.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketEventListener {
    @Autowired
    private ChatService chatService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event)
    {
        MessageHeaderAccessor accessor = NativeMessageHeaderAccessor.getAccessor(event.getMessage(), SimpMessageHeaderAccessor.class);
        GenericMessage<?> generic = (GenericMessage<?>) accessor.getHeader("simpConnectMessage");
        Map<String,Object> nativeHeaders = (Map<String, Object>) generic.getHeaders().get("nativeHeaders");
        String chatRoomId = ((List<String>) nativeHeaders.get("chatRoomId")).get(0);
        String sessionId = (String) generic.getHeaders().get("simpSessionId");

        chatService.connectUser(chatRoomId,sessionId);
    }
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        chatService.disconnectUser(sessionId);
    }
}
