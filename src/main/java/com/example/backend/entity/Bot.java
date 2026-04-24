package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bot")
@Data
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String personaDescription;
}
