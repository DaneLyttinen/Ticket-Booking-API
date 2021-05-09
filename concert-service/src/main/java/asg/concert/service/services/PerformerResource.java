package asg.concert.service.services;

import asg.concert.common.dto.PerformerDTO;
import asg.concert.service.domain.Performer;
import asg.concert.service.mapper.Mapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/concert-service/performers")
public class PerformerResource {

    private static Logger LOGGER = LoggerFactory.getLogger(PerformerResource.class);

    /**
     * Retrieves a performer with a specific id, and return to the
     * user in the form of a PerformerDTO. You don't have to be
     * authenticated (and hence don't have to be authorised) to do
     * this.
     * 
     * Throws Response.Status.NOT_FOUND if a performer doesn't
     * exist with that id.
     */
    @GET
    @Path("{id}")
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getPerformer(@PathParam("id") long id) {
        LOGGER.info("Retrieving performer with id: " + id);

        EntityManager em = PersistenceManager.instance().createEntityManager();

        Performer performer = null;

        try {
            em.getTransaction().begin();

<<<<<<< HEAD
            // Use the EntityManager to retrieve, persist or delete object(s).
            // Use em.find(), em.persist(), em.merge(), etc...
            performer = em.find(Performer.class, id, LockModeType.PESSIMISTIC_READ);
=======
            performer = em.find(Performer.class, id);
>>>>>>> master

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        if (performer == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Convert to DTO after confirming performer is not null
        PerformerDTO performerDTO = Mapper.convertObj(performer,  new TypeReference<PerformerDTO>(){});

        return Response.status(Response.Status.OK).entity(performerDTO).build();
    }

    /**
     * Retrieves all performers, and returns to the client in the 
     * form of a list of PerformerDTOs. You don't have to be authenticated 
     * (and hence don't have to be authorised) to do this.
     * 
     * There's no possibility error that can happen, i.e., if no performers
     * exist, this just returns a empty list. So no error can be thrown by
     * this method. (Unless there's a mistake with the code)
     */
    @GET
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response getAllPerformers(){
        LOGGER.info("Retrieving all performers: ");

        EntityManager em = PersistenceManager.instance().createEntityManager();

        List<PerformerDTO> performerDTOs = new ArrayList<>(); // having this here means that if the following doesn't retrieve any performers, an empty list is returned no problem

        try {
            em.getTransaction().begin();
<<<<<<< HEAD
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class)
                    .setLockMode(LockModeType.PESSIMISTIC_READ);
=======

            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
>>>>>>> master
            List<Performer> performers = performerQuery.getResultList();

            // Convert to list of performer DTOs
            for (Performer performer : performers){
                performerDTOs.add(Mapper.convertObj(performer,  new TypeReference<PerformerDTO>(){}));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        GenericEntity<List<PerformerDTO>> anEntity = new GenericEntity<List<PerformerDTO>>(performerDTOs) {};
        return Response.status(Response.Status.OK).entity(anEntity).build();
    }
}
