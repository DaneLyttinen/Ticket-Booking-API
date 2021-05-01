package asg.concert.service.services;

import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Performer;
import asg.concert.service.mapper.Mapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/concert-service")
public class PerformerResource {

    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    @GET
    @Path("performers/{id}")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getPerformer(@PathParam("id") long id) {
        LOGGER.info("Retrieving performer with id: " + id);
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Performer performer = null;
        PerformerDTO performerDTO = null;
        try {

            // Start a new transaction.
            em.getTransaction().begin();

            // Use the EntityManager to retrieve, persist or delete object(s).
            // Use em.find(), em.persist(), em.merge(), etc...
            performer = em.find(Performer.class, id);

            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        if (performer == null){
            return Response.status(404).build();
        }

        // Convert to DTO after confirming performer is not null
        performerDTO = Mapper.convertObj(performer,  new TypeReference<PerformerDTO>(){});

        return Response
                .status(200)
                .entity(performerDTO)
                .build();
    }

    @GET
    @Path("performers")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getAllPerformers(){
        LOGGER.info("Retrieving all performers: " );
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<PerformerDTO> performerDTOs = new ArrayList<>();
        try {

            // Start a new transaction.
            em.getTransaction().begin();
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
            List<Performer> performers = performerQuery.getResultList();

            for (Performer performer : performers){
                performerDTOs.add(Mapper.convertObj(performer,  new TypeReference<PerformerDTO>(){}));
            }

            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        return Response
                .status(200)
                .entity(performerDTOs)
                .build();
    }
}
