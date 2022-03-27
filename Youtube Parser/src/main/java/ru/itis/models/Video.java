package ru.itis.models;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, unique = true)
    private String url;

    @Column(name = "watch_id", nullable = false, unique = true)
    private String watchId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "published_at", nullable = false)
    private String publishedAt;

    @Column(name = "description", nullable = true)
    @Type(type = "text")
    private String description;

    @Column(name = "views_counter", nullable = true)
    private Long viewsCounter;

    @Column(name = "likes_counter", nullable = true)
    private Long likesCounter;
}
