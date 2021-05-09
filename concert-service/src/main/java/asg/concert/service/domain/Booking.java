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
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
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

    @ManyToOne
    public User user;

    // this shouldn't be used directly by our code because the database makes the id
    public Booking(long id, long concertId,LocalDateTime date, Set<Seat> seat, User user){ // constructor with all fields including id
        this(concertId, date, seat, user);
        this.id = id;
    }

    public Booking(long concertId, LocalDateTime date, Set<Seat> seat, User user) { // contructor with all fields
        this.concertId = concertId;
        this.date = date;
        this.seat = seat;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(String cookie){this.user = user;}
}
