package asg.concert.service.mapper;

import asg.concert.common.dto.*;
import asg.concert.service.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

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

}
