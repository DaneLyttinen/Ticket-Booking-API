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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/concert-service/concerts")
public class ConcertResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    /**
     * Retrieves a concert with a specific id, and return to the
     * user in the form of a ConcertDTO. You don't have to be
     * authenticated (and hence don't have to be authorised) to do
     * this.
     * 
     * Throws Response.Status.NOT_FOUND if a concert doesn't
     * exist with that id.
     */
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

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (concert == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(concertDTO).build();
    }

    /**
     * Retrieves all concerts, and returns to the client in the form 
     * of a list of ConcertDTOs. You don't have to be authenticated 
     * (and hence don't have to be authorised) to do this.
     * 
     * There's no possibility error that can happen, i.e., if no performers
     * exist, this just returns a empty list. So no error can be thrown by
     * this method. (Unless there's a mistake with the code)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConcerts(){
        LOGGER.info("Retrieving all concerts: " );

        EntityManager em = PersistenceManager.instance().createEntityManager();
        
        List<ConcertDTO> concertDTOs = new ArrayList<>(); // having this here means that if the following doesn't retrieve any concerts, an empty list is returned no problem

        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            
            // Convert to list of performer DTOs
            for (Concert concert : concerts){
                concertDTOs.add(Mapper.convertObj(concert, new TypeReference<ConcertDTO>(){}));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        GenericEntity<List<ConcertDTO>> anEntity = new GenericEntity<List<ConcertDTO>>(concertDTOs) {};
        return Response.status(Response.Status.OK).entity(anEntity).build();
    }

    /**
     * Retrieves all concerts, and returns to the client in the form 
     * of a list of ConcertSummaryDTOs. You don't have to be authenticated 
     * (and hence don't have to be authorised) to do this.
     * 
     * There's no possibility error that can happen, i.e., if no performers
     * exist, this just returns a empty list. So no error can be thrown by
     * this method. (Unless there's a mistake with the code)
     */
    @GET
    @Path("summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConcertSummaries() {
        LOGGER.info("Retrieving all concert summaries: " );

        EntityManager em = PersistenceManager.instance().createEntityManager();

        List<ConcertSummaryDTO> concertSummariesDTO = new ArrayList<>(); // having this here means that if the following doesn't retrieve any concerts, an empty list is returned no problem

        try {
            em.getTransaction().begin();

            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class); 
            List<Concert> concerts = concertQuery.getResultList();

            // Convert to a list of ConcertSummaryDTOs
            for (Concert concert: concerts) {
                concertSummariesDTO.add(Mapper.concertToSummaryDTO(concert));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        
        GenericEntity<List<ConcertSummaryDTO>> anEntity = new GenericEntity<List<ConcertSummaryDTO>>(concertSummariesDTO) {};
        return Response.status(Response.Status.OK).entity(anEntity).build();
    }

}
