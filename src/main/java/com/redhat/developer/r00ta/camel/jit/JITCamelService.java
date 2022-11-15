package com.redhat.developer.r00ta.camel.jit;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.RoutesLoader;
import org.apache.camel.support.ResourceHelper;

@Singleton
public class JITCamelService {

    private final ConcurrentHashMap<String, CamelContext> contexts = new ConcurrentHashMap<>();

    @Inject
    SessionService sessionService;

    public void sendMessage(String sessionId, String message) {
        CamelContext context = this.contexts.get(sessionId);
        ProducerTemplate template = context.createProducerTemplate();

        // TODO: refactor plz :) :)
        template.sendBody("direct:start", message);
    }

    public void removeRoute(String sessionId) {
        CamelContext context = contexts.get(sessionId);
        context.stop();
        contexts.remove(sessionId);
    }

    public void postRoute(String myRoute, String sessionId) {
        if (!sessionService.sessionExists(sessionId)) {
            throw new RuntimeException("Session " + sessionId + " does not exist.");
        }
        if (!myRoute.startsWith("from:\n" +
                                        "  uri: \"direct:start\"")) {
            throw new RuntimeException("Route must start with `from:\n  uri: \"direct:start\"`");
        }

        DefaultCamelContext context = new DefaultCamelContext();

        try {
            // TODO: refactor PLZ :) :) :)
            String prefix = "- route: \n    id: \"" + sessionId + "\"\n";

            myRoute = prefix + "    " + myRoute.replace("\n", "\n    ");

            ExtendedCamelContext extendedCamelContext = context.adapt(ExtendedCamelContext.class);
            if (extendedCamelContext.getLogListeners() == null || extendedCamelContext.getLogListeners().size() == 0) {
                extendedCamelContext.addLogListener(new MyLogListener(sessionService));
            }
            RoutesLoader loader = extendedCamelContext.getRoutesLoader();
            Resource resource = ResourceHelper.fromString("lololol.yaml", myRoute);
            loader.loadRoutes(resource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sessionService.sendToSession(sessionId, "Route has been created and it's ready to process events!");
        context.start();
        contexts.put(sessionId, context);
    }
}
