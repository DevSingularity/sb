package dev.singularity.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Maps 1:1 to the "urls" table (see V1__init_schema.sql). This replaces the
 * raw `INSERT INTO urls ...` / `SELECT ... FROM urls` strings in the
 * original urlService.js — Spring Data JPA generates that SQL for you from
 * the UrlMappingRepository interface instead.
 *
 * Note: `jakarta.persistence.*`, not `javax.persistence.*`. Every Java EE
 * API moved to the jakarta.* namespace back in Spring Boot 3; Spring Boot 4
 * finishes that migration (Jakarta EE 11). If you copy code from an old
 * tutorial and it won't compile, this is almost always why.
 */
@Entity
@Table(name = "urls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "long_url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String longUrl;

    @Column(name = "short_url", nullable = false, unique = true)
    private String shortUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UrlMapping(String longUrl, String shortUrl) {
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
    }

    /** JPA lifecycle callback — fires right before the first INSERT. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
