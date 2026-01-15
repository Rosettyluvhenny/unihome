package com.exe.unihome.websocket.handler;

import com.exe.unihome.chat.serviceImp.BotServiceImpl;
import com.exe.unihome.chat.serviceImp.ChatServiceImpl;
import com.exe.unihome.websocket.dto.ChatMessageResponse;
import com.exe.unihome.websocket.dto.SendChatMessageRequest;
import com.exe.unihome.websocket.dto.SystemMessage;
import com.exe.unihome.websocket.dto.WsMessage;
import com.exe.unihome.websocket.enums.WsMessageAction;
import com.exe.unihome.websocket.enums.WsMessageType;
import com.exe.unihome.websocket.session.WsSessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;

/**
 * Unified WebSocket handler for chat and notification messages.
 * Processes both CHAT and NOTIFICATION type messages.
 * All messages use a unified envelope structure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedWebSocketHandler extends TextWebSocketHandler {

  private final ObjectMapper objectMapper;
  private final WsSessionManager sessionManager;
  private final ChatServiceImpl chatService;
  private final BotServiceImpl botService;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String userId = (String) session.getAttributes().get("userId");
    if (userId != null) {
      // Ensure only one session per user (close old session if exists)
      Set<WebSocketSession> oldSessions = sessionManager.getSessions(userId);
      if (oldSessions != null && !oldSessions.isEmpty()) {
        for (WebSocketSession oldSession : oldSessions) {
          if (oldSession.isOpen()) {
            oldSession.close(CloseStatus.NORMAL);
          }
        }
      }
      sessionManager.addSession(userId, session);
      log.info("User {} connected via WebSocket", userId);
    } else {
      session.close(CloseStatus.BAD_DATA);
    }
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String userId = (String) session.getAttributes().get("userId");
    if (userId == null) {
      sendSystemError(session, 400, "User not authenticated");
      return;
    }

    try {
      WsMessage msg = objectMapper.readValue(message.getPayload(), WsMessage.class);
      JsonNode jsonNode = objectMapper.readTree(message.getPayload());
      WsMessageAction action = msg.getAction();
      WsMessageType type = msg.getType();


      if (type == null) {
        sendSystemError(session, 400, "Unknown message type: ");
        return;
      }

      if (action == null) {
        sendSystemError(session, 400, "Unknown action: ");
        return;
      }

      switch (type) {
        case CHAT:
          if (action.isChatAction()) {
            handleChatAction(action, jsonNode, session, userId);
          } else {
            sendSystemError(session, 400, "Invalid action for CHAT type: ");
          }
          break;
        case SYSTEM:
          sendSystemError(session, 400, "Cannot send SYSTEM type messages");
          break;
        default:
          sendSystemError(session, 400, "Unhandled message type: ");
      }
    } catch (Exception e) {
      log.error("Error handling WebSocket message", e);
      sendSystemError(session, 500, "Internal error: " + e.getMessage());
    }
  }

  private void handleChatAction(WsMessageAction action, JsonNode jsonNode, WebSocketSession session, String userId) throws Exception {
    switch (action) {
      case SEND_MESSAGE:
        handleSendMessage(jsonNode, session, userId);
        break;
      default:
        sendSystemError(session, 400, "Unknown chat action: " + action.getValue());
    }
  }


  /**
   * Handle SEND_MESSAGE action:
   * 1. Auto-determine room based on recipientId or botType
   * 2. Save message to database
   * 3. If receiver is online, push via WebSocket (type CHAT, action MESSAGE_RECEIVED)
   * 4. If receiver is offline, create a notification
   * 5. For BOT rooms, generate and send bot response
   */
  private void handleSendMessage(JsonNode payloadNode, WebSocketSession session, String userId) throws IOException {
    try {
      SendChatMessageRequest request = objectMapper.treeToValue(payloadNode.get("payload"), SendChatMessageRequest.class);

      if (request.getContent() == null || request.getContent().isBlank()) {
        sendSystemError(session, 400, "Content cannot be empty");
        return;
      }

      if (request.getRecipientId() == null && request.getBotType() == null) {
        sendSystemError(session, 400, "Either recipientId or botType must be provided");
        return;
      }

      if (request.getRecipientId() != null && request.getBotType() != null) {
        sendSystemError(session, 400, "Cannot specify both recipientId and botType");
        return;
      }


      // Handle user-to-user chat
      if (request.getRecipientId() != null) {
        handleUserToUserMessage(request, session, userId, userId);
      }
      // Handle user-to-bot chat
      else {
        handleUserToBotMessage(request, session, userId, userId);
      }

    } catch (Exception e) {
      log.error("Error handling send message", e);
      sendSystemError(session, 400, "Invalid message format");
    }
  }

  private void handleUserToUserMessage(SendChatMessageRequest request, WebSocketSession session, String userId, String senderId) throws IOException {
    // Auto-create or retrieve private room
    var chatRoom = chatService.sendPrivateMessage(senderId, request.getRecipientId(), request.getContent()).getRoomId();

    // Get the message that was just saved
    var message = chatService.getLastMessageByRoomId(chatRoom);
    if (message.isEmpty()) {
      return;
    }

    ChatMessageResponse response = ChatMessageResponse.builder()
      .id(message.get().getId())
      .roomId(chatRoom)
      .senderId(message.get().getSenderId())
      .senderType(message.get().getSenderType())
      .content(message.get().getContent())
      .createdAt(message.get().getCreatedAt())
      .build();

    String recipientId = request.getRecipientId();

    // Check if receiver is online
    if (sessionManager.isUserOnline(recipientId)) {
      // Push message via WebSocket
      pushChatMessage(recipientId, response);
    } else {
      // Create notification for offline user
      chatService.createChatNotification(recipientId, chatRoom, userId);
    }

    // Send confirmation to sender
    sendChatConfirmation(session, chatRoom);
  }

  private void handleUserToBotMessage(SendChatMessageRequest request, WebSocketSession session, String userId, String senderId) throws IOException {
    // Auto-create or retrieve bot room
    var botMessage = chatService.sendBotMessage(senderId, request.getBotType(), request.getContent());
    String roomId = botMessage.getRoomId();

    // Generate bot response
    String botResponse = botService.generateBotResponse(request.getContent());

    // Save bot message
    var savedBotMessage = chatService.saveBotResponse(roomId, botResponse);

    // Push bot response to user
    ChatMessageResponse response = ChatMessageResponse.builder()
      .id(savedBotMessage.getId())
      .roomId(roomId)
      .senderId(savedBotMessage.getSenderId())
      .senderType(savedBotMessage.getSenderType())
      .content(savedBotMessage.getContent())
      .createdAt(savedBotMessage.getCreatedAt())
      .build();

    pushChatMessage(userId, response);

    // Send confirmation to sender
    sendChatConfirmation(session, roomId);
  }

  private void pushChatMessage(String userId, ChatMessageResponse message) throws IOException {
    Set<WebSocketSession> sessions = sessionManager.getSessions(userId);
    if (sessions != null && !sessions.isEmpty()) {
      for (WebSocketSession session : sessions) {
        if (session.isOpen()) {
          WsMessage wsMessage = WsMessage.builder()
            .type(WsMessageType.CHAT)
            .action(WsMessageAction.MESSAGE_RECEIVED)
            .payload(message)
            .build();

          String json = objectMapper.writeValueAsString(wsMessage);
          session.sendMessage(new TextMessage(json));
        }
      }
    }
  }

  private void sendChatConfirmation(WebSocketSession session, String roomId) throws IOException {
    WsMessage confirmation = WsMessage.builder()
      .type(WsMessageType.CHAT)
      .action(WsMessageAction.MESSAGE_SENT)
      .payload(objectMapper.createObjectNode().put("roomId", roomId))
      .build();

    String json = objectMapper.writeValueAsString(confirmation);
    session.sendMessage(new TextMessage(json));
  }

  private void sendSystemError(WebSocketSession session, int code, String message) throws IOException {
    SystemMessage error = SystemMessage.builder()
      .code(code)
      .message(message)
      .build();

    WsMessage wsMessage = WsMessage.builder()
      .type(WsMessageType.SYSTEM)
      .action(WsMessageAction.ERROR)
      .payload(error)
      .build();

    String json = objectMapper.writeValueAsString(wsMessage);
    session.sendMessage(new TextMessage(json));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    String userId = (String) session.getAttributes().get("userId");
    if (userId != null) {
      sessionManager.removeSession(userId, session);
      log.info("User {} disconnected from WebSocket", userId);
    }
  }
}
