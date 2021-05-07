package asg.concert.service.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Concert;

@Path("/concert-service/subscribe")
public class SubscribeResource {
    
    @POST
    @Path("concertInfo")
    public Response subscribeConcertInfo(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        // Make sure client has valid cookie
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            
            em.getTransaction().begin();
            
            // Ensure concert with specified id exsists and at the specified date
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where id=" + concertInfoSubscriptionDTO.getConcertId(), Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            // if no concerts found with id or specified date, respond with error 400: bad request
            // there should only ever be one concert with a given id so just need concerts.get(0)
            if (concerts.size() == 0 || !concerts.get(0).getDates().contains(concertInfoSubscriptionDTO.getDate())) {
                em.getTransaction().commit();
                em.close(); 
                return Response.status(400).build();
            }


            em.getTransaction().commit();

        }
        finally {
            em.close();
        }


        return Response.status(404).build();
    }

}
