package asg.concert.service.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.*;

@Entity
@Table(name = "concert_summary")
public class ConcertSummary {
    @Id
    @Column(name = "concert_id")
    private Long id;
    private String title;
    @JsonSetter("imageName")
    @Column(name = "image_name")
    private String imageName;
    @OneToOne
    @MapsId
    @JoinColumn(name="concert_id")
    private Concert concert;

    public ConcertSummary() {
    }

    public ConcertSummary(Long id, String title, String imageName) {
        this.title = title;
        this.imageName = imageName;
        this.id = id;
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

    @JsonGetter("imageName")
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
