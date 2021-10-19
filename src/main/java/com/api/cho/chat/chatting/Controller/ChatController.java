package com.api.cho.chat.chatting.Controller;

import com.api.cho.chat.chatting.Domain.*;
import com.api.cho.chat.chatting.Service.ChatService;
import com.api.cho.chat.chatting.util.ServletUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @GetMapping("/join")
    @ResponseBody
    public DeferredResult<ChatResponse> joinRequest(){
        String sessionId = ServletUtil.getSession().getId();

        final ChatRequest user = new ChatRequest(sessionId);
        final DeferredResult<ChatResponse> deferredResult = new DeferredResult<>(null);
        chatService.joinChatRoom(user, deferredResult);

        deferredResult.onCompletion(() -> chatService.cancelChatRoom(user));
        deferredResult.onError((throwable) -> chatService.cancelChatRoom(user));
        deferredResult.onTimeout(()-> chatService.timeout(user));
        return deferredResult;

    }
    @GetMapping("/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelRequest(){
        String sessionId = ServletUtil.getSession().getId();
        final  ChatRequest user = new ChatRequest(sessionId);
        chatService.cancelChatRoom(user);
        return ResponseEntity.ok().build();
    }
    @MessageMapping("/chat.message/{chatRoomId}")
    public void sendMessage(@DestinationVariable("chatRoomId") String chatRoomId, @Payload ChatMessage chatMessage)
    {
        if(!StringUtils.hasText(chatRoomId) || chatMessage == null ){
            return;
        }
        if(chatMessage.getMessageType() == MessageType.CHAT)
        {
            chatService.sendMessage(chatRoomId,chatMessage);
        }
    }
}
