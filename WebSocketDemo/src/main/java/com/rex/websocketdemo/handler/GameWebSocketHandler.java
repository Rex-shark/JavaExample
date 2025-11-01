package com.rex.websocketdemo.handler;

import com.rex.websocketdemo.model.SocketMessageResponse;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    // 全部連線（用於全域廣播）
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    // 房間映射：roomId -> sessions
    private final ConcurrentMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    // sessionId -> roomId
    private final ConcurrentMap<String, String> sessionRoom = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = Optional.ofNullable(getQueryParam(session.getUri(), "id")).filter(s -> !s.isBlank()).orElse("default");
        sessions.add(session);
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRoom.put(session.getId(), roomId);

        String connected = "{\"type\":\"connected\",\"id\":\"" + session.getId() + "\",\"room\":\"" + escape(roomId) + "\"}";
        session.sendMessage(new TextMessage(connected));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = sessionRoom.getOrDefault(session.getId(), "default");
        String payload = message.getPayload();
        String out = "{\"type\":\"message\",\"from\":\"" + session.getId() + "\",\"room\":\"" + escape(roomId) + "\",\"data\":" + escapeJson(payload) + "}";
        broadcastToRoomExceptSender(roomId, session, out);
        session.sendMessage(new TextMessage("{\"type\":\"ack\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        String roomId = sessionRoom.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> room = rooms.get(roomId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
        }
    }

    // 全域廣播（全部房間）
    public void broadcastText(String text) {
        TextMessage tm = new TextMessage(text);
        sessions.forEach(s -> sendSafe(s, tm));
    }

    // 指定房間廣播（全部人）
    public void sendToRoom(String roomId, String text) {
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room == null){
            return;
        }
        TextMessage tm = new TextMessage(text);
        room.forEach(s -> sendSafe(s, tm));
    }

    // 指定房間廣播（全部人）
    public void sendToRoom(SocketMessageResponse res ,String roomId) {
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room == null){
            return;
        }
        System.out.println("res = " + res);
        TextMessage tm = new TextMessage(res.toJson());
        room.forEach(s -> sendSafe(s, tm));
    }

    // 指定房間廣播（排除發送者）
    private void broadcastToRoomExceptSender(String roomId, WebSocketSession sender, String text) {
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room == null) return;
        TextMessage tm = new TextMessage(text);
        room.forEach(s -> {
            if (!s.equals(sender)) sendSafe(s, tm);
        });
    }

    private void sendSafe(WebSocketSession s, TextMessage tm) {
        if (!s.isOpen()) { sessions.remove(s); removeFromRoomIfAny(s); return; }
        try {
            s.sendMessage(tm);
        } catch (IOException e) {
            try { s.close(); } catch (Exception ignored) {}
            sessions.remove(s);
            removeFromRoomIfAny(s);
        }
    }

    private void removeFromRoomIfAny(WebSocketSession s) {
        String roomId = sessionRoom.remove(s.getId());
        if (roomId == null) return;
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            room.remove(s);
            if (room.isEmpty()) rooms.remove(roomId);
        }
    }

    private String getQueryParam(URI uri, String key) {
        if (uri == null || uri.getQuery() == null) return null;
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String k = pair.substring(0, idx);
            if (key.equals(k)) return decode(pair.substring(idx + 1));
        }
        return null;
    }

    private String decode(String s) {
        try { return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception ignored) { return s; }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escapeJson(String s) {
        return "\"" + escape(s) + "\"";
    }
}
