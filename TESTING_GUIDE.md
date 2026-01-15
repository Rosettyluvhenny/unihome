# WebSocket Chat System - Testing Guide

## Prerequisites

1. Spring Boot application running on `http://localhost:8080`
2. PostgreSQL database with migrations applied (including V8__create_chat_tables.sql)
3. Two test users created with valid JWT tokens
4. Chat rooms created in the database

## Quick Setup for Testing

### 1. Create Test Users

Use the authentication endpoints to create two test users, or insert directly:

```sql
INSERT INTO users (id, full_name, email, password, status, role_name, created_at, updated_at)
VALUES ('user-1', 'Alice', 'alice@test.com', '<hashed-password>', 'ACTIVE', 'CUSTOMER', NOW(), NOW()),
       ('user-2', 'Bob', 'bob@test.com', '<hashed-password>', 'ACTIVE', 'CUSTOMER', NOW(), NOW());
```

### 2. Obtain JWT Tokens

Login with both users to get JWT tokens:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"<password>"}'
```

Response will contain access token. Save tokens as:

- `TOKEN_USER_1` for Alice
- `TOKEN_USER_2` for Bob

### 3. Create Chat Room

Create a PRIVATE chat room between the two users:

```sql
INSERT INTO chat_room (id, type, user_a_id, user_b_id, created_at)
VALUES ('room-1', 'PRIVATE', 'user-1', 'user-2', NOW());
```

Or create a BOT room for user testing:

```sql
INSERT INTO chat_room (id, type, user_a_id, bot_type, created_at)
VALUES ('room-bot-1', 'BOT', 'user-1', 'SUPPORT', NOW());
```

## Testing Scenarios

### Scenario 1: Real-Time Message Exchange (Both Users Online)

**Setup**: Two browser tabs or terminals

**Tab 1 (Alice - User 1)**:

```javascript
// Open browser DevTools → Console

// Connect to WebSocket
const ws1 = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_1');

// Listen for messages
ws1.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log('Received:', msg);
};

ws1.onerror = (error) => console.error('Error:', error);
ws1.onopen = () => console.log('Connected');
ws1.onclose = () => console.log('Disconnected');
```

**Tab 2 (Bob - User 2)**:

```javascript
const ws2 = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_2');

ws2.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log('Received:', msg);
};

ws2.onerror = (error) => console.error('Error:', error);
ws2.onopen = () => console.log('Connected');
ws2.onclose = () => console.log('Disconnected');
```

**Send Message from Tab 1 (Alice)**:

```javascript
// In Tab 1 console, send a message
ws1.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'room-1',
    content: 'Hello Bob, how are you?'
  }
}));
```

**Expected Results**:

- Tab 1 (Alice) receives: `{type: "CHAT", action: "MESSAGE_SENT", payload: {roomId: "room-1"}}`
- Tab 2 (Bob) receives:
  `{type: "CHAT", action: "MESSAGE_RECEIVED", payload: {id: "...", roomId: "room-1", senderId: "user-1", senderType: "USER", content: "Hello Bob, how are you?", createdAt: "..."}}`

**Send Reply from Tab 2 (Bob)**:

```javascript
ws2.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'room-1',
    content: 'I am doing great, thanks for asking!'
  }
}));
```

**Expected Results**:

- Tab 2 (Bob) receives: `{type: "CHAT", action: "MESSAGE_SENT", ...}`
- Tab 1 (Alice) receives: `{type: "CHAT", action: "MESSAGE_RECEIVED", ...}`

### Scenario 2: Offline User - Notification Creation

**Setup**: Only one user online

**Disconnect Tab 2 (Bob)**:

```javascript
ws2.close();
```

**Send Message from Tab 1 (Alice)**:

```javascript
ws1.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'room-1',
    content: 'Are you there Bob?'
  }
}));
```

**Expected Results**:

- Tab 1 (Alice) receives: `{type: "CHAT", action: "MESSAGE_SENT", ...}`
- Database: New notification created for user-2 with type=CHAT, channel=WEBSOCKET
- Query to verify:

```sql
SELECT *
FROM notification
WHERE user_id = 'user-2'
  AND type = 'CHAT'
ORDER BY created_at DESC LIMIT 1;
```

**Bob Reconnects**:

```javascript
const ws2 = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_2');
// Should receive notification via existing notification system
```

### Scenario 3: Bot Message Exchange

**Setup**: BOT room created, one user online

**Connect User to Bot Room**:

```javascript
const wsBot = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_1');

wsBot.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  console.log('Received:', msg);
};

wsBot.onopen = () => console.log('Connected to Bot');
```

**Send Message to Bot**:

```javascript
wsBot.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'room-bot-1',
    content: 'What is your support policy?'
  }
}));
```

**Expected Results**:

- Receives: `{type: "CHAT", action: "MESSAGE_SENT", ...}`
- Receives: `{type: "CHAT", action: "MESSAGE_RECEIVED", payload: {senderType: "BOT", content: "...", ...}}`
- Database: Two messages saved (user message + bot response)

**Query to verify**:

```sql
SELECT *
FROM chat_message
WHERE room_id = 'room-bot-1'
ORDER BY created_at;
```

### Scenario 4: Load Chat History via REST API

**Get List of Rooms**:

```bash
curl -X GET http://localhost:8080/chat/rooms \
  -H "Authorization: Bearer TOKEN_USER_1"
```

**Response**:

```json
[
  {
    "id": "room-1",
    "type": "PRIVATE",
    "userAId": "user-1",
    "userBId": "user-2",
    "createdAt": "2025-01-14T10:00:00Z"
  },
  {
    "id": "room-bot-1",
    "type": "BOT",
    "userAId": "user-1",
    "botType": "SUPPORT",
    "createdAt": "2025-01-14T09:00:00Z"
  }
]
```

**Get Paginated Messages from Room**:

```bash
curl -X GET "http://localhost:8080/chat/rooms/room-1/messages?page=0&size=10" \
  -H "Authorization: Bearer TOKEN_USER_1"
```

**Response**:

```json
{
  "content": [
    {
      "id": "msg-1",
      "roomId": "room-1",
      "senderId": "user-1",
      "senderType": "USER",
      "content": "Hello Bob, how are you?",
      "createdAt": "2025-01-14T10:30:00Z"
    },
    {
      "id": "msg-2",
      "roomId": "room-1",
      "senderId": "user-2",
      "senderType": "USER",
      "content": "I am doing great!",
      "createdAt": "2025-01-14T10:31:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 2
  }
}
```

### Scenario 5: Session Management (One Session Per User)

**Connect Same User Twice**:

```javascript
// Tab 1
const ws1a = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_1');

// Later, Tab 2 (same user)
const ws1b = new WebSocket('ws://localhost:8080/ws?token=TOKEN_USER_1');

// Check that ws1a is closed
setTimeout(() => {
  console.log('ws1a connected:', ws1a.readyState === WebSocket.OPEN); // false
  console.log('ws1b connected:', ws1b.readyState === WebSocket.OPEN); // true
}, 100);
```

**Expected Result**:

- First connection closes automatically
- Second connection succeeds
- Only one active session per user

### Scenario 6: Error Handling

**Send Invalid Message**:

```javascript
ws1.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'room-1'
    // Missing: content
  }
}));
```

**Expected Result**:

```json
{
  "type": "SYSTEM",
  "action": "ERROR",
  "payload": {
    "code": 400,
    "message": "Invalid message payload"
  }
}
```

**Send to Non-Existent Room**:

```javascript
ws1.send(JSON.stringify({
  type: 'CHAT',
  action: 'SEND_MESSAGE',
  payload: {
    roomId: 'invalid-room-id',
    content: 'test'
  }
}));
```

**Expected Result**:

```json
{
  "type": "SYSTEM",
  "action": "ERROR",
  "payload": {
    "code": 404,
    "message": "Room not found"
  }
}
```

## Database Verification Queries

### View All Messages in a Room

```sql
SELECT *
FROM chat_message
WHERE room_id = 'room-1'
ORDER BY created_at DESC;
```

### View Chat Rooms for a User

```sql
SELECT *
FROM chat_room
WHERE user_a_id = 'user-1'
   OR user_b_id = 'user-1'
ORDER BY created_at DESC;
```

### View Recent Notifications for a User

```sql
SELECT *
FROM notification
WHERE user_id = 'user-2'
  AND type = 'CHAT'
ORDER BY created_at DESC LIMIT 10;
```

### Count Active Sessions (via app logs)

Watch application logs for:

```
User [userId] connected via WebSocket
User [userId] disconnected from WebSocket
```

## Performance Testing

### Load Test: Send Multiple Messages

```javascript
// Send 100 messages in rapid succession
for (let i = 0; i < 100; i++) {
  ws1.send(JSON.stringify({
    type: 'CHAT',
    action: 'SEND_MESSAGE',
    payload: {
      roomId: 'room-1',
      content: `Message ${i}`
    }
  }));
}

// Verify all messages received and saved
setTimeout(() => {
  console.log('Check database for 100 new messages');
}, 5000);
```

## Troubleshooting

### WebSocket Connection Fails

- **Issue**: `401 Unauthorized`
- **Solution**: Verify JWT token is valid and not expired

- **Issue**: Connection closes immediately
- **Solution**: Check JwtHandshakeInterceptor logs; JWT extraction may have failed

### No Real-Time Messages Received

- **Issue**: Message saved but not received in real-time
- **Solution**:
  - Check if receiver session is still open: `ws.readyState === WebSocket.OPEN`
  - Check server logs for errors
  - Verify room ID is correct

### Notifications Not Created

- **Issue**: Offline user doesn't receive notification
- **Solution**:
  - Verify receiver's session is closed
  - Check NotificationService logs
  - Verify notification channel is WEBSOCKET

### Session Management Issues

- **Issue**: Multiple sessions for same user active
- **Solution**: Verify WsSessionManager is being used in UnifiedWebSocketHandler

## Monitoring

### Check Active WebSocket Connections

```java
// In application, inject WsSessionManager
private final WsSessionManager sessionManager;

// Get all sessions
Map<String, WebSocketSession> allSessions = sessionManager.getAllSessions();
System.out.

println("Active sessions: "+allSessions.size());
```

### Log Message Flow

Check application logs for patterns:

```
User [userId] connected via WebSocket
[TRACE] Saving message to room [roomId]
[TRACE] User [receiverId] is online, pushing message
[TRACE] User [receiverId] is offline, creating notification
[TRACE] User [userId] disconnected from WebSocket
```

## Cleanup

After testing, you can clean up test data:

```sql
-- Delete test messages
DELETE
FROM chat_message
WHERE room_id IN ('room-1', 'room-bot-1');

-- Delete test notifications
DELETE
FROM notification
WHERE user_id IN ('user-1', 'user-2');

-- Delete test rooms
DELETE
FROM chat_room
WHERE id IN ('room-1', 'room-bot-1');

-- Delete test users
DELETE
FROM users
WHERE id IN ('user-1', 'user-2');
```

