package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import asg.concert.common.jackson.LocalDateTimeDeserializer;
import asg.concert.common.jackson.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;

@Entity
@Table(name="concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //This will set a arbitrary length for the column so it can be inserted, otherwise it would have to pass as varchar(255) which the blurbs will not.
    @Column(columnDefinition="text")
    private String blurb;
    private String title;
    @Column(name = "IMAGE_NAME")
    private String imageName;
    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID"))
    @Column(name = "DATE")
    private Set<LocalDateTime> dates;

    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(
                    name = "CONCERT_ID",
                    referencedColumnName = "ID"
            ),
            inverseJoinColumns = @JoinColumn(
                    name="PERFORMER_ID",
                    referencedColumnName = "ID"
            )
    )
    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Performer> performers = new HashSet<>();

    public Concert() {} // JPA needs a blank constructor

    // this shouldn't be used directly by our code because the database makes the id
    public Concert(Long id, String title, String imageName, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.dates = dates;
        this.performers = performer;
        this.blurb = blurb;
    }

    public Concert(String title, String imageName, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this(null, title, imageName, dates, performer, blurb);
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class) // This is used by our Mapper class
    @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getBlurb() {
        return blurb;
    }
}
