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

@ServerEndpoint("/websockets/{sessionId}")
@ApplicationScoped
public class SessionSocket {

    @Inject
    SessionService sessionService;

    @Inject
    JITCamelService camelService;

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        sessionService.addSession(sessionId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("sessionId") String sessionId) {
        camelService.removeRoute(sessionId);
        sessionService.removeSession(sessionId);
    }

    @OnError
    public void onError(Session session, @PathParam("sessionId") String sessionId, Throwable throwable) {
        sessionService.removeSession(sessionId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sessionId") String sessionId) {
        if (message.equalsIgnoreCase("_ready_")) {
            System.out.println("ready");
        } else {
            camelService.sendMessage(sessionId, message);
        }
    }
}