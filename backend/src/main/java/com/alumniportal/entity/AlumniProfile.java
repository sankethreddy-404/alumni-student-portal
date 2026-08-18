package com.alumniportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String company;
    private String domain;

    @Column(length = 1000)
    private String skills; // comma-separated

    private String location;
    private Integer graduationYear;
    private String currentRole;
    private Integer experience; // years

    @Column(length = 2000)
    private String achievements;

    @Column(length = 2000)
    private String bio;

    private String linkedinUrl;
    private String resumeFilePath;

    @Builder.Default
    private boolean availableForMentorship = false;

    private LocalDateTime lastVerifiedAt;

    @Builder.Default
    private Integer profileCompleteness = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastVerifiedAt = LocalDateTime.now();
    }
}
