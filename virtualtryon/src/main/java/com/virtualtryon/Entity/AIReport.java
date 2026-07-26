package com.virtualtryon.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    private String occasion;

    private String colorSuggestion;

    @OneToOne
    @JoinColumn(name = "generated_image_id")
    private GeneratedImage generatedImage;

}
