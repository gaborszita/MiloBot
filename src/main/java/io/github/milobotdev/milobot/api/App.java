package io.github.milobotdev.milobot.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/json")
@Interceptors(io.github.milobotdev.milobot.api.AuthorizedAPI.class)
public class App {

    //@Inject
    //AuthorizedAPI authorizedAPI;

    @Inject
    private DependencyInjectionService depserv;

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Person hello() {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        depserv.getstg();
        depserv.num();
        return new Person();
    }

    @GET
    @Path("/getjwt")
    public Response getjwt(@QueryParam("data") String data) {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        depserv.num();
        System.out.println(data);
        try {
            return Response.ok().entity(JWTManager.generateJWT(data)).build();
        } catch (JWTException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/processjwt")
    public Response processjwt(@QueryParam("jwt") String jwt) {
        //  System.out.println(httpSession.getId());
        //System.out.println(num.getValue().getNum());
        depserv.num();
        System.out.println(jwt);
        try {
            return Response.ok().entity(JWTManager.decryptJWT(jwt)).build();
        } catch (JWTException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}