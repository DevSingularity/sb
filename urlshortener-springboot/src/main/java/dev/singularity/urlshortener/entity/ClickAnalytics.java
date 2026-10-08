package dev.singularity.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps 1:1 to the "click_analytics" table. One row is written per redirect,
 * mirroring recordClickInDb() from urlService.js.
 */
@Entity
@Table(name = "click_analytics")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickAnalytics {

    @Id
    private UUID id;

    @Column(name = "short_url", nullable = false)
    private String shortUrl;

    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ip;

    @Column(name = "user_agent")
    private String userAgent;

    private String referer;
}
