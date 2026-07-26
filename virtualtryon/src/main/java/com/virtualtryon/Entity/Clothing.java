package com.virtualtryon.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Clothing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String clothName;

    private String clothType;

    private String color;

    private LocalDateTime uploadedAt;

    private String imagePath;

    @OneToMany(mappedBy = "clothing")
    private List<ColorPreference> colorPreference;
}
