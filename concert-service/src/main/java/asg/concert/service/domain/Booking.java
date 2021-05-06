package asg.concert.service.domain;

import asg.concert.common.jackson.LocalDateTimeDeserializer;
import asg.concert.common.jackson.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Bookings")
public class Booking {

    @Id
    // remove this if using explicit id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    public LocalDateTime date;
    private long concertId;
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "BOOKING_SEAT",
            joinColumns = @JoinColumn(
                    name = "BOOKING_ID",
                    referencedColumnName = "ID"
            ),
            inverseJoinColumns = @JoinColumn(
                    name="SEAT_ID",
                    referencedColumnName = "ID"
            )
    )
    public Set<Seat> seat = new HashSet<>();


    public String cookie;

    public Booking(Long id, long concertId,LocalDateTime date, Set<Seat> seat, String cookie){
        this.id = id;
        this.concertId = concertId;
        this.date = date;
        this.seat = seat;
        this.cookie = cookie;
    }

    public Booking(long concertId, LocalDateTime date, Set<Seat> seat, String cookie) {
        this(null, concertId, date, seat, cookie);
    }

    public Booking(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<Seat> getSeat() {
        return seat;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie){this.cookie = cookie;}
}
