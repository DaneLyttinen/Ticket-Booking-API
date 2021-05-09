package asg.concert.service.domain;

import asg.concert.common.types.Genre;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="performers")
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String image_name;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Column(columnDefinition="text")
    private String blurb;

    public Performer() {} // JPA likes blank constructor

    // this shouldn't be used directly by our code because the database makes the id
    public Performer(Long id, String name, String imageName, Genre genre, String blurb) {
        this.id = id;
        this.name = name;
        this.image_name = imageName;
        this.genre = genre;
        this.blurb = blurb;
    }

    public Performer(String name, String imageName, Genre genre, String blurb) {
        this(null, name, imageName,genre, blurb);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return image_name;
    }

    public void setImageName(String imageName) {
        this.image_name = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getBlurb() {
        return blurb;
    }
}
