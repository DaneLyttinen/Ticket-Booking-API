package asg.concert.service.services;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.common.dto.ConcertSummaryDTO;
import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Performer;
import asg.concert.service.mapper.Mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/concert-service/concerts")
public class ConcertResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    @GET
    @Path("{id}")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response retrieveConcert(@PathParam("id") long id) {
        LOGGER.info("Retrieving concert with id: " + id);
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Concert concert = null;
        ConcertDTO concertDTO = null;
        try {
            // Start a new transaction.
            em.getTransaction().begin();
            
            // Use the EntityManager to retrieve, persist or delete object(s).
            // Use em.find(), em.persist(), em.merge(), etc...
            concert = em.find(Concert.class, id);

            // Had to move back the mapper as otherwise we would get a LazyLoadException with Dates.
            // As we are in a try block, if concert couldn't be found it will just stay as null
            concertDTO = Mapper.convertObj(concert,  new TypeReference<ConcertDTO>(){});

            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        if (concert == null){
            return Response.status(404).build();
        }

        return Response
                .status(200)
                .entity(concertDTO)
                .build();
    }

    @GET
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getAllConcerts(){
        LOGGER.info("Retrieving all concerts: " );
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<ConcertDTO> concertDTOs = new ArrayList<>();
        try {

            // Start a new transaction.
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            
            for (Concert concert : concerts){
                concertDTOs.add(Mapper.convertObj(concert, new TypeReference<ConcertDTO>(){}));
            }

            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        return Response
                .status(200)
                .entity(concertDTOs)
                .build();
    }

    @GET
    @Path("summaries")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getConcertSummaries() {
        LOGGER.info("Retrieving all concert summaries: " );
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<ConcertSummaryDTO> concertSummariesDTO = new ArrayList<>();

        try {
            // start a new transaction
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            // create a summary for each concert
            for (Concert concert: concerts) {
                concertSummariesDTO.add(Mapper.concertToSummaryDTO(concert));
            }

            // Commit the transaction.
            em.getTransaction().commit();


        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        
        return Response
                .status(200)
                .entity(concertSummariesDTO)
                .build();
    }

}
