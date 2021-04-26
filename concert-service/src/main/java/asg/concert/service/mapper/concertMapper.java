package asg.concert.service.mapper;

import asg.concert.common.dto.ConcertDTO;
import asg.concert.common.jackson.LocalDateTimeSerializer;
import asg.concert.service.domain.Concert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class concertMapper {

    // Generic mapper, Object is the original object and the ref is what you want to convert the object to.
    public <T> T convertObjTOXXX(Object o, TypeReference ref){
        ObjectMapper mapper = new ObjectMapper();
        return (T) mapper.convertValue(o, ref);
    }

//    public ConcertDTO convertToDto(Concert concert) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(concert);
//        ConcertDTO concertDTO = mapper.readValue(json, ConcertDTO.class);
//        return concertDTO;
//    }
//
//    public Concert convertToDomain(ConcertDTO concertDTO) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(concertDTO);
//        Concert concert = mapper.readValue(json, Concert.class);
//        return concert;
//    }
}
