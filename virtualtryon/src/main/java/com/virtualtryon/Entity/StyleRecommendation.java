package com.virtualtryon.Entity;

import jakarta.persistence.*;

@Entity
public class StyleRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Integer overallRating;

    private String outfitType;

    private String occasion;

    private String accessories;

    private String recommendedColors;

    @ManyToOne
    @JoinColumn(name = "generated_image_id")
    private GeneratedImage generatedImage;

}
