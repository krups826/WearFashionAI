package com.virtualtryon.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private Integer age;

    private String gender;

    private LocalDateTime createdAt;

    private Boolean enabled;

    private String theme;

    @OneToMany(mappedBy = "user")
    private List<History> history;

    @OneToMany(mappedBy = "user")
    private List<Favorite> favorite;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<ColorPreference> colorPreference;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private VerificationToken verificationToken;
}

