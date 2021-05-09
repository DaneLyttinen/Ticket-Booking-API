package asg.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
@Entity
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime date;
	private boolean isBooked;
	private String label;
	private BigDecimal price;

	public Seat() {} // JPA likes blank constructor

	// this shouldn't be used directly by our code because the database makes the id
	public Seat(Long id, String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.id = id;
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this(null,label, isBooked, date, price);
	}

	public long getId() {
		return this.id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setBooked(boolean booked) {
		isBooked = booked;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}
}
