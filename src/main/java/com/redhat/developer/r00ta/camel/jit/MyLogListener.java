package com.redhat.developer.r00ta.camel.jit;

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
            this.sessionService.sendToSession(exchange.getFromRouteId(), "LOGGER NAME: " + camelLogger.getLog().getName() + ". MESSAGE: " + exchange.getIn().getBody().toString());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        return String.format("%s: %s", exchange.getFromRouteId(), exchange.getMessage().getBody());
    }
}