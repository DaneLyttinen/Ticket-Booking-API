package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name="concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    //This will set a arbitrary length for the column so it can be inserted, otherwise it would have to pass as varchar(255) which the blurbs will not.
    @Column(columnDefinition="text")
    private String blurb;
    private String image_name;
    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID"))
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>();

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
    @OneToMany(cascade = CascadeType.PERSIST)
    private Set<Performer> performers = new HashSet<>();

    public Concert(Long id, String title, String image_name, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this.id = id;
        this.title = title;
        this.image_name = image_name;
        this.dates = dates;
        this.performers = performer;
        this.blurb = blurb;
    }

    public Concert(String title,String image_name, Set<LocalDateTime> dates, Set<Performer> performer, String blurb) {
        this(null, title,image_name, dates, performer, blurb);
    }

    public Concert() {
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_name() {
        return image_name;
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
