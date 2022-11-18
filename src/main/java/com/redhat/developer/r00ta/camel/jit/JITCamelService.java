package com.redhat.developer.r00ta.camel.jit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.redhat.developer.r00ta.camel.jit.models.JITMessage;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.RoutesLoader;
import org.apache.camel.support.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JITCamelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JITCamelService.class);

    @Inject
    CamelContext camelContext;

    @Inject
    SessionService sessionService;

    @PostConstruct
    void init(){
        ExtendedCamelContext extendedCamelContext = camelContext.adapt(ExtendedCamelContext.class);
        extendedCamelContext.addLogListener(new MyLogListener(sessionService));
    }

    public void sendMessage(String sessionId, JITMessage jitMessage) {
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.sendBodyAndHeaders("direct:" + sessionId, jitMessage.getBody(), jitMessage.getHeaders());
    }

    public void removeRoute(String sessionId) {
        try {
            camelContext.getRouteController().stopRoute(sessionId);
            camelContext.removeRoute(sessionId);
        } catch (Exception e) {
            LOGGER.warn("Failed to stop route " + sessionId);
        }
    }

    public void postRoute(String myRoute, String sessionId) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new RuntimeException("Session " + sessionId + " does not exist.");
        }

        if (!myRoute.startsWith("from:\n" +
                                        "  uri: \"direct:start\" # Edit below!")) {
            throw new RuntimeException("Route must start with `from:\n  uri: \"direct:start\"`");
        }

        myRoute = myRoute.replace("direct:start", "direct:" + sessionId);

        try {
            // TODO: refactor PLZ :) :) :)
            String prefix = "- route: \n    id: \"" + sessionId + "\"\n";
            myRoute = prefix + "    " + myRoute.replace("\n", "\n    ");

            ExtendedCamelContext extendedCamelContext = camelContext.adapt(ExtendedCamelContext.class);
            RoutesLoader loader = extendedCamelContext.getRoutesLoader();
            Resource resource = ResourceHelper.fromString("input.yaml", myRoute);
            loader.loadRoutes(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sessionService.sendToSession(sessionId, "\ud83d\ude00 Route has been created and it's ready to process events!");
    }
}
