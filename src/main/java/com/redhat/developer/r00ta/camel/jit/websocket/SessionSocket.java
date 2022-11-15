package com.redhat.developer.r00ta.camel.jit.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.redhat.developer.r00ta.camel.jit.JITCamelService;
import com.redhat.developer.r00ta.camel.jit.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/websockets/{sessionId}")
@ApplicationScoped
public class SessionSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionSocket.class);

    @Inject
    SessionService sessionService;

    @Inject
    JITCamelService camelService;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        LOGGER.info(String.format("New websocket '%s'", sessionId));
        sessionService.addSession(sessionId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("sessionId") String sessionId) {
        LOGGER.info(String.format("Removing websocket '%s'", sessionId));
        camelService.removeRoute(sessionId);
        sessionService.removeSession(sessionId);
    }

    @OnError
    public void onError(Session session, @PathParam("sessionId") String sessionId, Throwable throwable) {
        LOGGER.info(String.format("Error on websocket '%s'. Closing.", sessionId));
        onClose(session, sessionId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sessionId") String sessionId) {
        if (message.equalsIgnoreCase("_ready_")) {
            LOGGER.info(String.format("websocket '%s' is ready", sessionId));
        } else {
            camelService.sendMessage(sessionId, message);
        }
    }
}