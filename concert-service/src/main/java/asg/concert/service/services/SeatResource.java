package asg.concert.service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.LoggerFactory;

import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Seat;
import asg.concert.service.mapper.Mapper;

import org.slf4j.Logger;

@Path("/concert-service/seats")
public class SeatResource {
    private static Logger LOGGER = LoggerFactory.getLogger(SeatResource.class);

    @GET
    @Path("{dateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeats(@PathParam("dateTime") String dateTimeString, @QueryParam("status") String status) {
        // Convert dateTimeString to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);

        LOGGER.info("Getting '" + status + "' seats with time: " + dateTime.toString());

        // change query based on specified status
        String queryBookedStatus = "";
        if (status.equals("Booked")) { queryBookedStatus = " and isBooked=true"; }
        else if (status.equals("Unbooked")) { queryBookedStatus = " and isBooked=false"; }

        List<SeatDTO> seatDTOs = new ArrayList<>();

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Start a new transaction.
            em.getTransaction().begin();
            
            // get seats with specified date and status
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where Date='" + dateTime.toString() + "'" + queryBookedStatus, Seat.class);
            for (Seat seat: seatQuery.getResultList()) {
                seatDTOs.add(Mapper.convertObj(seat, new TypeReference<SeatDTO>(){}));
            }

            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }

        return Response
                .status(200)
                .entity(seatDTOs)
                .build();
    }
}
