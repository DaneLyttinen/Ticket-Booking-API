package asg.concert.service.mapper;

import asg.concert.common.dto.*;
import asg.concert.service.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Mapper {
    public static <T> T convertObj(Object o, TypeReference ref){
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.convertValue(o, ref);
    }

    public static ConcertSummaryDTO concertToSummaryDTO(Concert concert) {
        return new ConcertSummaryDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getImage_name()
        );
    }

    public static BookingDTO convertBooking(Booking booking){
        List<SeatDTO> seatDTOSet = new ArrayList<>();
        for (Seat seat : booking.getSeat()){
            SeatDTO seatDTO = Mapper.convertObj(seat, new TypeReference<SeatDTO>(){});
            seatDTOSet.add(seatDTO);
        }
        BookingDTO bookingDTO = Mapper.convertObj(booking, new TypeReference<BookingDTO>(){});
        bookingDTO.setSeats(seatDTOSet);
        return  bookingDTO;
    }

}
