package com.redhat.developer.r00ta.camel.jit.api;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.developer.r00ta.camel.jit.JITCamelService;

@Path("/camel/jit")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class JITCamelAPI {

    @Inject
    JITCamelService jitCamelService;

    @POST
    @Path("/{routeId}")
    public Response postRoute(@PathParam("routeId") String routeId, String dsl) {
        jitCamelService.postRoute(dsl, routeId);
        return Response.ok().build();
    }
}
