package asg.concert.service.services;

import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.ConcertInfoNotificationDTO;
import asg.concert.common.dto.ConcertInfoSubscriptionDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.Booking;
import asg.concert.service.domain.Concert;
import asg.concert.service.domain.Seat;

@Path("/concert-service/subscribe")
public class SubscribeResource {

    private static Logger LOGGER = LoggerFactory.getLogger(SubscribeResource.class);
    private static Map<ConcertInfoSubscriptionDTO, AsyncResponse> concertInfoSubs = new Hashtable<>();
    
    @POST
    @Path("concertInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribeConcertInfo(ConcertInfoSubscriptionDTO concertInfoSubscriptionDTO, @Suspended AsyncResponse sub, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId) {
        if (clientId == null) {
            // Responses are made via the AsyncResponse object, so we return nothing
            sub.resume(Response.status(401).build());
            return;
        }

        LOGGER.info("Attempting to subscribe to info for Concert (id: " + concertInfoSubscriptionDTO.getConcertId() + ", at: " + concertInfoSubscriptionDTO.getDate().toString() + ", when " + concertInfoSubscriptionDTO.getPercentageBooked() + "% booked.)");
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            
            em.getTransaction().begin();
            
            // Ensure concert with specified id exsists and at the specified date
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c where id=" + concertInfoSubscriptionDTO.getConcertId(), Concert.class).setLockMode(LockModeType.PESSIMISTIC_READ);
            List<Concert> concerts = concertQuery.getResultList();

            // if no concerts found with id or specified date, respond with error 400: bad request
            // there should only ever be one concert with a given id so just need concerts.get(0)
            if (concerts.size() == 0 || !concerts.get(0).getDates().contains(concertInfoSubscriptionDTO.getDate())) {
                em.getTransaction().commit();
                em.close(); 
                
                // Respond via AsyncResponse object
                sub.resume(Response.status(400).build());
                return;
            }

            em.getTransaction().commit();

        }
        finally {
            em.close();
        }

        // The requested concert is valid, add subscriber to subs list
        LOGGER.info("Adding to subscriptions.");
        synchronized (concertInfoSubs) {
            concertInfoSubs.put(concertInfoSubscriptionDTO, sub);
        }
    }

    public static void bookingMade(Booking booking) {
        // A booking has been made, check if there are any subscriptions that want to know about this particular concert
        for (ConcertInfoSubscriptionDTO subDTO: concertInfoSubs.keySet()) {
            // Booking concert matches subscribed concert and booked percentace is over threshold
            if (booking.getConcertId() == subDTO.getConcertId() && booking.getDate().equals(subDTO.getDate())) {
                // Get the seats from the concert
                EntityManager em = PersistenceManager.instance().createEntityManager();
                String dateTimeStr = booking.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                List<Seat> seats;

                try {
                    em.getTransaction().begin();

                    TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where date='" + dateTimeStr + "'", Seat.class).setLockMode(LockModeType.PESSIMISTIC_READ);
                    seats = seatQuery.getResultList();

                    em.getTransaction().commit();
                }
                finally {
                    em.close();
                }

                // Get number of booked seats
                int bookedSeats = 0;

                for (Seat seat: seats) {
                    if (seat.isBooked()) {
                        bookedSeats++;
                    }
                }
                
                // Get percentage booked
                float percentageBooked =  (float) (100. * ((float) bookedSeats / (float) seats.size())); 

                // Notify subscriber if over threshold
                if (percentageBooked >= subDTO.getPercentageBooked()) {
                    LOGGER.info("Notifying subscriber of concert " + booking.getConcertId() + "(" + booking.getDate().toString() + ") " + (int) percentageBooked + "% > " + subDTO.getPercentageBooked() + "%, " + (seats.size() - bookedSeats) + " seats left.");
                    // Create notificationDTO and send to subscriber
                    ConcertInfoNotificationDTO notification = new ConcertInfoNotificationDTO(seats.size() - bookedSeats);
                    concertInfoSubs.get(subDTO).resume(Response.status(200).entity(notification).build()); 
                    // Remove subscriber from subs to avoid duplicate subscribers hanging around
                    concertInfoSubs.remove(subDTO);
                }
            }
        }
    }
}
