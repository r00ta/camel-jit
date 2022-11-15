package com.redhat.developer.r00ta.camel.jit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    Map<String, Session> sessions = new ConcurrentHashMap<>(); // (2)

    public void addSession(String id, Session session) {
        this.sessions.put(id, session);
    }

    public void removeSession(String id) {
        this.sessions.remove(id);
    }

    public boolean sessionExists(String id) {
        return sessions.containsKey(id);
    }

    public void sendToSession(String sessionId, String message) {
        sessions.get(sessionId).getAsyncRemote().sendObject(message, result -> {
            if (result.getException() != null) {
                LOGGER.warn("Unable to send message: " + result.getException() + ". SessionId='" + sessionId + "'.");
            }
        });
    }
}
