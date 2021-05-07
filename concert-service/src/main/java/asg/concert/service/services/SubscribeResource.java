package asg.concert.service.services;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/concert-service/subscribe/concertInfo")
public class SubscribeResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response subscribe(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, @CookieParam("auth")Cookie clientID){
        if (clientID == null){
            return Response.status(401).build();
        }
        return Response.status(200).build();
    }
}
