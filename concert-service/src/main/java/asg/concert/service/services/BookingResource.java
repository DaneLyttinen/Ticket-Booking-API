package asg.concert.service.services;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import asg.concert.common.dto.BookingDTO;
import asg.concert.common.dto.ConcertDTO;
import asg.concert.service.domain.Booking;
import asg.concert.service.mapper.Mapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Seat;

@Path("/concert-service/bookings")
public class BookingResource {

    private static Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);
    private AtomicLong idCounter = new AtomicLong();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bookingRequestDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        // Make sure client has valid cookie
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }

        LOGGER.info("Attempting booking for concert (id: " + bookingRequestDTO.getConcertId() + ") on " + bookingRequestDTO.getDate().toString() + " for seats " + bookingRequestDTO.getSeatLabels().toString());
        long id = 0;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        Booking booking = new Booking();

        try {

            em.getTransaction().begin();

            // Ensure concert with specified id exsists and at the specified date
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where id=" + bookingRequestDTO.getConcertId(), Concert.class);
            List<Concert> concerts = concertQuery.getResultList();

            // if no concerts found with id or specified date, respond with error 400: bad request
            // there should only ever be one concert with a given id so just need concerts.get(0)
            if (concerts.size() == 0 || !concerts.get(0).getDates().contains(bookingRequestDTO.getDate())) {
                em.getTransaction().commit();
                em.close(); 
                return Response.status(400).build();
            }

            // Create string representation of seat labels for query. Example: "('A1', 'B2', 'C3')" 
            String requestLabelsList = "('" + String.join("', '", bookingRequestDTO.getSeatLabels()) + "')";
            // Format datetime into appropriate string for request
            String requestDateTime = bookingRequestDTO.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // get all seats in booking query
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where date='" + requestDateTime + "' and label in " + requestLabelsList, Seat.class);
            List<Seat> seats = seatQuery.getResultList();
            Set<Seat> seatsSet = new HashSet<>();
            // if any of the seats are already booked, respond with error 403: forbidden
            for (Seat seat: seats) {
                if (seat.isBooked()) {
                    LOGGER.info("Booking failed! Seat " + seat.getLabel() + " is already booked.");
                    
                    em.getTransaction().commit();
                    em.close(); 
                    return Response.status(403).build();
                }
            }

            // otherwise book the seats
            for (Seat seat: seats) {
                seat.setBooked(true);
                em.persist(seat);
                seatsSet.add(seat);
            }

            // TODO: store this in database or something
            booking.seat = seatsSet;
            booking.cookie = clientId.getValue();
            booking.date = bookingRequestDTO.getDate();
            booking.setConcertId(bookingRequestDTO.getConcertId());
            //Booking booking = new Booking(id, bookingRequestDTO.getConcertId(), bookingRequestDTO.getDate(), seatsSet, clientId.getValue());
            em.persist(booking);
            id = booking.getId();
            em.getTransaction().commit();
        }
        finally {
            em.close();    
        }

        // Notify subscribers of this booking
        SubscribeResource.bookingMade(booking);


        return Response
                .created(URI.create("/concert-service/bookings/" + id))
                .status(201)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsersBookings(@CookieParam(Config.CLIENT_COOKIE) Cookie clientId){
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }
        EntityManager em = PersistenceManager.instance().createEntityManager();
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        try {

            // Start a new transaction.
            em.getTransaction().begin();
            TypedQuery<Booking> bookingQuery = em.createQuery("select s from Booking s where cookie='" + clientId.getValue() + "'", Booking.class);
            List<Booking> bookings = bookingQuery.getResultList();
            for (Booking booking : bookings){
                BookingDTO bookingDTO = Mapper.convertBooking(booking);
                bookingDTOS.add(bookingDTO);
            }
            em.getTransaction().commit();

        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        return  Response
                .status(200)
                .entity(bookingDTOS)
                .build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingById(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        if (!LoginResource.isCookieValid(clientId)) {
            return Response.status(401).build();
        }

        // TODO: responds with a list of BookingDTOs for the user
        // We could get the user based off the clientId if we store the cookies in the database
        // with a reference to the user the cookie is for
        Booking booking = null;
        BookingDTO bookingDTO = null;
        EntityManager em = PersistenceManager.instance().createEntityManager();
        LOGGER.info("Attempting to get booking with user " + clientId.getValue() );
        try {

            // Start a new transaction.
            em.getTransaction().begin();
            booking = em.find(Booking.class, id);
            LOGGER.info("Attempting to get booking with (cookie: " + booking.getCookie() + ") with user " + clientId.getValue() );
            if (!booking.getCookie().equals(clientId.getValue())){
                return Response.status(403).build();
            }
            bookingDTO = Mapper.convertBooking(booking);
            // Commit the transaction.
            em.getTransaction().commit();

        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        if (booking == null){
            return Response.status(404).build();
        }

        return Response
                .status(200)
                .entity(bookingDTO)
                .build();
    }
}
