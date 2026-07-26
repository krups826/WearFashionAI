package com.virtualtryon.Entity;

import jakarta.persistence.*;

@Entity
public class ColorPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String selectedColor;

    @ManyToOne
    @JoinColumn(name = "clothing_id")
    private Clothing clothing;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



}
