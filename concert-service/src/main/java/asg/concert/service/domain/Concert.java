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
    private Set<Performer> performers; 
    // indeed, Concert and Performers have a many to many relationship. 
    // Note: this is unidirectional, and because this class does the storing, this class is the 'owner' to JPA (hence we don't need mappedBy)
    // RE: CascadeType.PERSIST; this means when we persist the owner (which is this), the other side will be persisted too, which is what we want. 
    // Note, We haven't done CascadeType.REMOVE as well, because a Performer shouldnt be removed when a Concert that has it is removed (which is what CascadeType.REMOVE does)

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

    // Getters and Setters for every field

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return this.imageName;
    } 

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return this.blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<Performer> getPerformers() {
        return this.performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    public Set<LocalDateTime> getDates() { // this was given to us; really dropping the hint that they want us to use Set<LocalTimeDate> ?
        return this.dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    // Nice to have, since these fields are collections

    public void addPerformer(Performer performer) {
        this.performers.add(performer);
    }

    public void addDate(LocalDateTime date) {
        this.dates.add(date);
    }

}
