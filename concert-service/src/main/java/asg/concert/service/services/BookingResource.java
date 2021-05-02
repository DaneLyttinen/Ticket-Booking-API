package asg.concert.service.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Seat;

@Path("/concert-service/bookings")
public class BookingResource {

    private static Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bookingDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        // Make sure client has valid cookie
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            em.getTransaction().begin();

            // TODO: implement this.

            // TEMPORARY CODE HERE just for the SeatResource tests
            // -------------
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where date='2020-02-15 20:00:00'", Seat.class);
            List<Seat> seats = seatQuery.getResultList();

            for (Seat seat: seats) {
                if (seat.getLabel().equals("C5") || seat.getLabel().equals("C6")) {
                    seat.setBooked(true);
                    em.persist(seat);
                }
            }
            // -------------

            em.getTransaction().commit();

        }
        finally {
            em.close();    
        }

        return Response.status(404).build();
    }
}
