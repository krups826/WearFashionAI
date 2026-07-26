package com.virtualtryon.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class GeneratedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String outputImage;

    private LocalDateTime generatedAt;

    private String status;

    @OneToOne(mappedBy = "generatedImage" , cascade = CascadeType.ALL)
    private AIReport aiReport;

    @OneToOne(mappedBy = "generatedImage" , cascade = CascadeType.ALL)
    private Favorite favorite;

    @OneToMany(mappedBy = "generatedImage")
    private List<StyleRecommendation> styleRecommendations;

    @ManyToOne
    @JoinColumn(name = "clothing_id")
    private Clothing clothing;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
