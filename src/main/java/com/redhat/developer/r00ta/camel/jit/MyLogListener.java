package com.redhat.developer.r00ta.camel.jit;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.spi.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogListener implements LogListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyLogListener.class);

    private final SessionService sessionService;

    public MyLogListener(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String onLog(Exchange exchange, CamelLogger camelLogger, String message) {
        try {
            String socketMessage = String.format("\uD83D\uDCE9 %s: %s. Headers: %s.", camelLogger.getLog().getName(),
                                          message.startsWith("Exchange[") ?  exchange.getMessage().getBody().toString() : message,
                                                 message.startsWith("Exchange[") ? exchange.getMessage().getHeaders() : null);
            this.sessionService.sendToSession(exchange.getFromRouteId(), socketMessage);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return String.format("New log processed for route '%s'", exchange.getFromRouteId()); // Do not log user's data on the server.
    }

    private String serializaHeaders(Map<String, Object> headers) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(headers);
    }
}