package asg.concert.service.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import asg.concert.common.dto.BookingDTO;
import asg.concert.common.dto.ConcertDTO;
import asg.concert.service.mapper.Mapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Seat;

@Path("/concert-service/bookings")
public class BookingResource {

    private static Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);
    private AtomicLong idCounter = new AtomicLong();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bookingDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        // Make sure client has valid cookie
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }
        BookingDTO abookingDTO = null;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            em.getTransaction().begin();

            // TODO: implement this.

            // TEMPORARY CODE HERE just for the SeatResource tests
            // -------------
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where date='"+bookingDTO.getDate()+"'", Seat.class);
            List<Seat> seats = seatQuery.getResultList();

            for (Seat seat: seats) {
                if (bookingDTO.getSeatLabels().contains(seat.getLabel())) {
                    seat.setBooked(true);
                    em.persist(seat);
                }
            }
            // -------------
            abookingDTO =  Mapper.convertObj(bookingDTO,  new TypeReference<BookingDTO>(){});
            em.getTransaction().commit();

        }
        finally {
            em.close();    
        }
        var id = idCounter.incrementAndGet();
        return Response
                .created(URI.create("/bookings/" + id))
                .status(201)
                .build();
    }

    // TODO: implement this.
//    @GET
//    @Path("{id}")
//    public Response getBooking(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId){
//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        try {
//
//            // Start a new transaction.
//            em.getTransaction().begin();
//            Booking booking = em.find(booking.class, id);
//            if (booking. cookie != clientId) {
//                throw new WebApplicationException(Response.Status.NOT_FOUND);
//            }
//
//            // Use the EntityManager to retrieve, persist or delete object(s).
//            // Use em.find(), em.persist(), em.merge(), etc...
//
//            // Commit the transaction.
//            em.getTransaction().commit();
//
//        } finally {
//            // When you're done using the EntityManager, close it to free up resources.
//            em.close();
//        }
//
//        return Response
//                .status(204)
//                .build();
//    }
}
