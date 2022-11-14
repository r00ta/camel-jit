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

    @Inject
    SessionService sessionService;

    private final ConcurrentHashMap<String, CamelContext> contexts = new ConcurrentHashMap<>();

    public void sendMessage(String sessionId, String message) {
        CamelContext context = this.contexts.get(sessionId);
        ProducerTemplate template = context.createProducerTemplate();

        // TODO: refactor plz :) :)
        template.sendBody("direct:start", message);
    }

    public void removeRoute(String uuid) {
        CamelContext context = contexts.get(uuid);
        context.stop();
        contexts.remove(uuid);
    }

    public void postRoute(String myRoute, String uuid) {
        if (!sessionService.sessionExists(uuid)) {
            throw new RuntimeException("Session " + uuid + " does not exist.");
        }
        if (!myRoute.startsWith("from:\n" +
                                        "  uri: \"direct:start\"")){
            throw new RuntimeException("Route must start with `from:\n  uri: \"direct:start\"`");
        }

        DefaultCamelContext context = new DefaultCamelContext();

        try {
            // TODO: refactor PLZ :) :) :)
            String prefix = "- route: \n    id: \"" + uuid + "\"\n";

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

        sessionService.sendToSession(uuid, "Route has been created and it's ready to process events!");
        context.start();
        contexts.put(uuid, context);
    }
}

/*
 * from:
 *     uri: "timer:tick"
 *     parameters:
 *       period: 1000
 *     steps:
 *       - set-body:
 *           constant: "Welcome to Apache Camel KA"
 *       - to:
 *           uri: "log:info"
 */
