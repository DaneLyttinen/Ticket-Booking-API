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
@Table(name="bookings") // don't need to name the table this, but nice to follow the convention of the others
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    public LocalDateTime date;
    private long concertId;
    @OneToMany(fetch = FetchType.EAGER)
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
    public Set<Seat> seats;

    @ManyToOne()
    public User user;

    // this shouldn't be used directly by our code because the database makes the id
    public Booking(long id, long concertId,LocalDateTime date, Set<Seat> seats, User user){ // constructor with all fields including id
        this(concertId, date, seats, user);
        this.id = id;
    }

    public Booking(long concertId, LocalDateTime date, Set<Seat> seats, User user) { // contructor with all fields
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
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

    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // This is used by our Mapper class (i.e. we don't give our client instances of Booking domain model)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // ^
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
