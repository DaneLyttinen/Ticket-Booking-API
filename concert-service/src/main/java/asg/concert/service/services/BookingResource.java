package asg.concert.service.services;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import asg.concert.common.dto.BookingDTO;
import asg.concert.service.domain.Booking;
import asg.concert.service.domain.User;
import asg.concert.service.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.BookingRequestDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Seat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/concert-service/bookings")
public class BookingResource {

    private static Logger LOGGER = LoggerFactory.getLogger(BookingResource.class);

    /**
     * The user gives information regarding what they want to book: concert id they want to book,
     * the date they want to book, and the seats they want to book (via a BookingDTO) and the
     * webserver makes the booking for them (if possible).
     * 
     * Note, there's only one venue, so concert id is not necessary to book - 
     * however, it's a good failsafe to check that the user is booking as intended.
     * 
     * Steps:
     * 1) Authenticate the user (i.e. confirm who they are) via the cookie sent (again, this 
     * is thanks to login giving them this cookie). 
     * 
     * This is done via LoginResource.authenticateFromCookie, which throws 
     * Response.Status.UNAUTHORISED if authentication can't happen.
     * 
     * 2) Check the concert is running on this date (again, this is a fail safe, because this
     * isn't necessary to make a booking, because there's only one venue).
     * 
     * If not, throw Response.Status.BAD_REQUEST
     * 
     * 3) Get the seat objects that the client wants to book from the database; check that they
     * all are "not booked" (we can't override a booking, nor can we only book some - called
     * a partial booking).
     * 
     * If not, throw Response.Status.FORBIDDEN
     * 
     * 4) Check that all the seats the client asked for exist (i.e. the client didn't give gibberish
     * labels).
     * 
     * If not, throw Response.Status.BAD_REQUEST
     * 
     * 5) Make the booking: set seats to booked, and instantiate and persist a booking object.
     * A booking object is a means to store the information of the user who made the booking
     * and the seats that were booked (and other booking information). 
     * 
     * Why a booking object? It is nice to have (rather than storing the information of who made
     * the booking via storing the booked seats with the user), because there might be other booking
     * information in the future (e.g. payment information), so it seems nice to have a booking 
     * object who's responsibility is to store all information regarding a booking.
     * 
     * 6) Give a URI in which the client can recieve a BookingDTO containing info about the booking
     * they just made. Note that getting from a booking (domain model) object to a BookingDTO is easy,
     * hence further justifying making a booking object.
     * 
     * The tests don't state what this has to be, however, the tests say that "/bookings" gets you
     * all the bookings [of the user making the request; the user being identified by the cookie they send].
     * Hence, to follow convention as per concert and performer, let there be "/bookings/{id}".
     * To reiterate, this was a creative decision.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bookingRequestDTO, @CookieParam(Config.CLIENT_COOKIE_NAME) Cookie aCookie) {
        LOGGER.info(String.format("Attempting booking for concert (id: %d) on date '%s' for seats '%s'", bookingRequestDTO.getConcertId(), bookingRequestDTO.getDate().toString(), bookingRequestDTO.getSeatLabels().toString()));

        // 1) Authenticate the user via the cookie sent
        User authenticatedUser = LoginResource.authenticateFromCookie(aCookie);
        User userMakingBooking = authenticatedUser; // for clarity


        // assign booking request DTO's info to variables
        long desiredConcertIdToBook = bookingRequestDTO.getConcertId();
        LocalDateTime desiredDateToBook = bookingRequestDTO.getDate();
        List<String> desiredSeatLabelsToBook = bookingRequestDTO.getSeatLabels();

        
        EntityManager em = PersistenceManager.instance().createEntityManager();

        Booking aBooking;
        
        try {
            em.getTransaction().begin();
            // 2) Check the concert with the passed id exists, and that it is running on this date
            Concert desiredConcertToBook = em.find(Concert.class, desiredConcertIdToBook, LockModeType.PESSIMISTIC_READ);

            if (desiredConcertToBook == null) { // also necessary to catch this here, so that we know the below can run (could to a try/except/etc. instead)
                LOGGER.info(String.format("There does not exist a concert with the id '%d'. Throwing Response.Status.BAD_REQUEST", desiredConcertIdToBook));
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            em.getTransaction().commit();
            if (! desiredConcertToBook.getDates().contains(desiredDateToBook)) {
                LOGGER.info("The concert that the client is intending to book seats for is NOT scheduled on this date. Throwing Response.Status.BAD_REQUEST");
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            em.getTransaction().begin();
            // 3) Get desired seats to book from databsae, and check that they are all "not booked"
            String requestDateTime = bookingRequestDTO.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // Format datetime into appropriate string for request
            String requestLabelsListAsString = "('" + String.join("', '", bookingRequestDTO.getSeatLabels()) + "')"; // Create string representation of seat labels for query. Example: "('A1', 'B2', 'C3')" 
            String queryString = String.format("select s from Seat s where date = '%s' and label in %s", requestDateTime, requestLabelsListAsString);
            TypedQuery<Seat> seatQuery = em.createQuery(queryString, Seat.class );
            List<Seat> desiredSeatsToBookFoundInDatabase = seatQuery.getResultList();

            for (Seat seat: desiredSeatsToBookFoundInDatabase) {
                if (seat.isBooked()) {
                    LOGGER.info(String.format("A seat that the client desires to book (seat label : %s) is booked already! We can't book a seat that is already booked, nor can we book any seats if we can't book them all (i.e. aka, we can't make a 'partial booking'). So throwing Response.Status.FORBIDDEN", seat.getLabel()));
                    throw new WebApplicationException(Response.Status.FORBIDDEN);
                }
            }

            // 4) Check that all seats the client asked for exist
            if (desiredSeatsToBookFoundInDatabase.size() < desiredSeatLabelsToBook.size()) {
                LOGGER.info("Couldn't find all the seats that the client desired to book, i.e. the client gave at least one gibberish seat label");
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            // 5) Make the booking: set seats to booked, and instantiate and persist a booking object
            for (Seat seat : desiredSeatsToBookFoundInDatabase) {
                seat.setBooked(true);
                em.merge(seat);
                LOGGER.info(String.format("Updated seat with id '%d' in database; it now has its isBooked field assigned true!", seat.getId()));

            }

            Set<Seat> desiredSeatsToBookFoundInDatabaseSet = new HashSet<>(desiredSeatsToBookFoundInDatabase); // Booking stores a set of seats, which makes sense - and we just got it out of the database as a list. This could be problematic.
            aBooking = new Booking(desiredConcertIdToBook, desiredDateToBook, desiredSeatsToBookFoundInDatabaseSet, userMakingBooking);
            
            em.persist(aBooking);
            LOGGER.info(String.format("Persisted (i.e. put in database for the first time) booking with id '%d'!", aBooking.getId()));
            em.getTransaction().commit();
        }
        finally {
            em.close();    
        }
        
        // 6) Give a URI in which the client can recieve a BookingDTO containing info about the booking they just made
        URI bookingIsAtURI = URI.create("/concert-service/bookings/" + aBooking.getId());
        LOGGER.info(String.format("To see BookingDTO for this booking, go to url '%s'. Returning this location in HTTP reply message.", bookingIsAtURI));

        // Notify subscribers of this booking
        SubscribeResource.bookingMade(aBooking);

        return Response.status(Response.Status.CREATED).location(bookingIsAtURI).build();
    }

    /**
     * Given a booking id, return a BookingDTO containing information about that booking.
     * 
     * Note: only the user that made the booking (colloquially, we can say they "own" the booking),
     * can view that booking information (the BookingDTO that comes out of this request).
     * 
     * Steps:
     * 1) Authenticate user (i.e. confirm who they are), again via cookie sent. 
     * 
     * Accomplished via LoginResource.authenticateFromCookie, which throws 
     * Response.Status.UNAUTHORISED if authentication can't happen.
     * 
     * 2) Get the booking information from the database (which will make the BookingDTO), and again,
     * using a Booking object stores all this booking information.
     * 
     * If booking information not found (i.e. booking object does not exist with that id),
     * throw Response.Status.BAD_REQUEST
     * 
     * 3) Before going further, and it's our first chance (this could've been included
     * in the above database query), check that the authenticated user who made this
     * request is authorised to see the booking information (i.e. see the BookingDTO). Here,
     * the only person authorised to see the booking information is the person who made that
     * booking; however, in the future, this could be extended to an admin user.
     * 
     * If not authorised, throw Response.Status.FORBIDDEN (more like "not authorised")
     * 
     * 4) Instantiate and return BookingDTO
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveSingleBooking(@PathParam("id") long id, @CookieParam(Config.CLIENT_COOKIE_NAME) Cookie aCookie) {
        LOGGER.info(String.format("Attempting to view booking (id: %d). Note, can only do this if user authenticated by passed cookie is authorised", id));
        
        // 1) Authenticate the user via the cookie sent
        User authenticatedUser = LoginResource.authenticateFromCookie(aCookie);


        EntityManager em = PersistenceManager.instance().createEntityManager();

        BookingDTO bookingDTO = null;

        try {
            em.getTransaction().begin();

            // 2) Get the booking information from the database 
            Booking aBooking = em.find(Booking.class, id, LockModeType.PESSIMISTIC_READ); // note, this has been eagerly fetched, so the below need not be here

            if (aBooking == null) {
                LOGGER.info("Booking does not exist with this id");
                throw new WebApplicationException(Response.Status.BAD_REQUEST); // I don't think this is tested, but yep, a fitting status for this situation
            }

            // 3)  Authorise
            if (authenticatedUser.equals(aBooking.getUser())) {
                LOGGER.info("Authorised. Client is the same person who made this booking");
            } else {// ah: so we use forbidden when not authorised (And we used UNAUTHORISED when not authenticated) -- well, that's a shame.
                LOGGER.info(String.format("Not Authorised. Client (user: '%s') is trying to access the booking of another user (user: '%s'). Client is only authorised to access their bookings. Throwing Response.Status.FORBIDDEN", authenticatedUser, aBooking.getUser()));
                throw new WebApplicationException(Response.Status.FORBIDDEN); // you're trying to access the booking of another user ("you're not authorised to do that")
            }

            // 4) Instantiate and return BookingDTO
            bookingDTO = Mapper.convertBooking(aBooking);

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response.status(Response.Status.OK).entity(bookingDTO).build();
    }


    /**
     * This retrieves all bookings [of the user authenticated by the cookie sent in the request], and
     * puts them in the form of a list of BookingDTOs and replies.
     * 
     * Steps:
     * 1) Authenticate user (i.e. confirm who they are), again via cookie sent. 
     * 
     * Accomplished via LoginResource.authenticateFromCookie, which throws 
     * Response.Status.UNAUTHORISED if authentication can't happen.
     * 
     * 2) Get all bookings done by that user. Again, we have Booking objects 
     * which represent a booking in the database. I.e., query database for all booking
     * objects for that user.
     * 
     * 3) Convert to a list of BookingDTOs (again, each BookingDTO represents a booking),
     * and return that.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllBookings(@CookieParam(Config.CLIENT_COOKIE_NAME) Cookie aCookie){
        LOGGER.info("Attempting to view all bookings, for the user that the passed cookie authenticates");

        // 1) Authenticate the user via the cookie sent
        User authenticatedUser = LoginResource.authenticateFromCookie(aCookie);

        EntityManager em = PersistenceManager.instance().createEntityManager();

        List<BookingDTO> bookingDTOs = new ArrayList<>(); // i.e. if the follow try/finally doesn't yield any booking dtos, then an empty list is returned

        try {
            em.getTransaction().begin();

            // 2) Get all bookings done by that user
            String queryString = String.format("select b from Booking b where b.user = '%d'", authenticatedUser.getId());
            TypedQuery<Booking> bookingQuery = em.createQuery(queryString, Booking.class);
            List<Booking> bookings = bookingQuery.getResultList();

            // 3) Convert to a list of BookingDTOs
            BookingDTO aBookingDTO;
            for (Booking booking : bookings){
                aBookingDTO = Mapper.convertBooking(booking);
                bookingDTOs.add(aBookingDTO);
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        GenericEntity<List<BookingDTO>> anEntity = new GenericEntity<List<BookingDTO>>(bookingDTOs) {}; // so type information is preserved in HTTP response
        return Response.status(Response.Status.OK).entity(anEntity).build();
    }


}
