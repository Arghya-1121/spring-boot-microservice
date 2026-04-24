package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

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
    private Integer depthLevel = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
