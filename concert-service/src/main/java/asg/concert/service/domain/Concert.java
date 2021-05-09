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
    @JsonSetter("imageName")
    @Column(name = "image_name")
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
    public Concert(Long id, String title, String image_name, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = image_name;
        this.dates = dates;
        this.performers = performer;
        this.blurb = blurb;
    }

    public Concert(String title,String image_name, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this(null, title,image_name, dates, performer, blurb);
    }

    public void setImage_name(String image_name) {
        this.imageName = image_name;
    }

    @JsonGetter("imageName")
    public String getImage_name() {
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

    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
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
