package ru.itis.models;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", referencedColumnName = "id", nullable = false)
    private Video video;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "representer", nullable = false)
    private String representer;
}
