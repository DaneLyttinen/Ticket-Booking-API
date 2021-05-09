package asg.concert.service.domain;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="bookings") // to follow convention of the rest of the tables
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    public LocalDateTime date;
    private long concertId; // perhaps it'd be cooler if it stored the concert object itself


    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER) // indeed booking has a many to many relationship with seat (booking objects have a collection of seat objects; seat object can only have one booking object, because the seat is at a specific time. If not, this would be double booking)
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
    ) // This isn't in db_init.sql, i.e. it's not a requirement, this is just nice to have.
    public Set<Seat> seats;

    @ManyToOne // indeed, Booking has a many to one relationship with user (booking objects have one user objects; user objects can have many booking objects) 
    public User user;

    // this shouldn't be used directly by our code because the database makes the id
    public Booking(long id, long concertId,LocalDateTime date, Set<Seat> seat, User user){ // constructor with all fields including id
        this(concertId, date, seat, user);
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
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getConcertId() {
        return this.concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<Seat> getSeats() {
        return this.seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user){
        this.user = user;
    }
}
