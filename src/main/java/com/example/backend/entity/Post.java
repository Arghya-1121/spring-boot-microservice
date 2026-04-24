package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthorType authorType;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Column(nullable = false)
    private int likeCount = 0;
}
