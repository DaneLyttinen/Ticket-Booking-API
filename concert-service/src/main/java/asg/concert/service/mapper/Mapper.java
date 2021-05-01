package asg.concert.service.mapper;

import asg.concert.common.dto.*;
import asg.concert.service.domain.*;

import java.util.ArrayList;

public class Mapper {
    public static ConcertDTO concertToDTO(Concert concert) {
        ConcertDTO concertDTO = new ConcertDTO(
            concert.getId(),
            concert.getTitle(),
            concert.getImage_name(),
            concert.getBlurb()
        );

        ArrayList<PerformerDTO> concertPerformers = new ArrayList<>();
        for (Performer performer: concert.getPerformers()) {
            concertPerformers.add(performerToDTO(performer));
        }

        concertDTO.setPerformers(concertPerformers);
        concertDTO.setDates(new ArrayList<>(concert.getDates()));

        return concertDTO;
    }

    public static ConcertSummaryDTO concertToSummaryDTO(Concert concert) {
        return new ConcertSummaryDTO(
            concert.getId(),
            concert.getTitle(),
            concert.getImage_name()
        );
    }

    public static PerformerDTO performerToDTO(Performer performer) {
        return new PerformerDTO(
            performer.getId(),
            performer.getName(),
            performer.getImageName(),
            performer.getGenre(),
            performer.getBlurb()
        );
    }

    public static SeatDTO seatToDTO(Seat seat) {
        return new SeatDTO(
            seat.getLabel(),
            seat.getPrice()
        );
    }

    public static UserDTO userToDTO(User user) {
        return new UserDTO(
            user.getUsername(),
            user.getPassword()
        );
    }
}
