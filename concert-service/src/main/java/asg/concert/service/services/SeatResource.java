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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.LoggerFactory;

import asg.concert.common.dto.SeatDTO;
import asg.concert.service.domain.Seat;
import asg.concert.service.mapper.Mapper;

import asg.concert.service.jaxrs.LocalDateTimeParam;
import asg.concert.common.types.BookingStatus;

import org.slf4j.Logger;

@Path("/concert-service/seats")
public class SeatResource {
    private static Logger LOGGER = LoggerFactory.getLogger(SeatResource.class);

     /**
     * This retrieves the seats on a particular day, with a particular status. Obviously, we 
     * just have one venue, but when we have more, it could be nice to extend this to 
     * getting seats at a particular venue.
     * 
     * Note: you don't have to be authenticated (and hence don't have to be authorised) because
     * SeatDTOs (which is what is returned) aren't seen as sensitive data - i.e., as opposed
     * to booking info which is sensitive data: who booked the seat, and in the future, probably,
     * payment info.
     * 
     * The date is passe as LocalDateTime (well, actually wrapped), and set is passed as a
     * StatusType enum ?value?. 
     * 
     * Steps:
     * 1) Query database for seats which are on the passed: particular datetime and particular status
     * 
     * 2) Convert to list of SeatDTOs
     * 
     * Note: I presume database is good at querying, rather than doing it with objects outside
     * of the database. However, this makes code dependent how we've set up the mapping to JPA 
     * - i.e., to make this codenot dependent on the name of the column in the database would 
     * be to do this querying with objects. Maybe the performance benefit is worth it? Something
     * to think about for future projects. 
     */
    @GET
    @Path("{dateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeats(@PathParam("dateTime") LocalDateTimeParam dateTime, @QueryParam("status") BookingStatus status) {
        LocalDateTime aLocalDateTime = dateTime.getLocalDateTime();
        LOGGER.info(String.format("Getting seats which are on datetime '%s' and status '%s'", aLocalDateTime.toString(), status.toString()));

        // 1) Query database for seats which are on the passed: particular datetime and particular status

        //String dateInDatabaseFormat = dateLocalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " "); // format gives: "2020-02-15T20:00:00", replace gives "2020-02-15 20:00:00". yes, could've just not used LocalDateTimeParam if thi was what we were going to do
        String queryStringSeatOnDate = String.format("select s from Seat s where s.date = '%s'", aLocalDateTime.toString());

        String queryStringSeatOnDateWithStatus;
        if (status.equals(BookingStatus.Any)) {
            queryStringSeatOnDateWithStatus = queryStringSeatOnDate;
        } else if (status.equals(BookingStatus.Booked)) {
            queryStringSeatOnDateWithStatus = queryStringSeatOnDate + " and s.isBooked = 'TRUE'";
        } else if (status.equals(BookingStatus.Unbooked)) {
            queryStringSeatOnDateWithStatus = queryStringSeatOnDate + " and s.isBooked = 'FALSE'";
        } else {
            LOGGER.info(String.format("Status '%s' was not expected. Returning bad request status code", status));
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        List<SeatDTO> seatDTOs = new ArrayList<>();

        EntityManager em = PersistenceManager.instance().createEntityManager();

        List<Seat> retrievedSeats = null;

        try {
            em.getTransaction().begin();
            
            TypedQuery<Seat> seatQuery = em.createQuery(queryStringSeatOnDateWithStatus, Seat.class);
            retrievedSeats = seatQuery.getResultList();

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // 2) Convert to list of SeatDTOs
        List<SeatDTO> retrievedSeatsDTO = new ArrayList<>();
        for (Seat seat : retrievedSeats) { // doing it this way, so we only iterate over all seats once, the consequence is that we are continually searching desiredSeatLabelsToBook (as opposed to the other way around; doing it this way around because desiredSeatLabelsToBook should be smaller or the same size)
            SeatDTO aSeatDTO = Mapper.convertObj(seat, new TypeReference<SeatDTO>(){});
            retrievedSeatsDTO.add(aSeatDTO);
        }

        GenericEntity<List<SeatDTO>> anEntity = new GenericEntity<List<SeatDTO>>(retrievedSeatsDTO) {}; // So type information is preserved
        return Response.status(Response.Status.OK).entity(retrievedSeatsDTO).build();
    }
}
