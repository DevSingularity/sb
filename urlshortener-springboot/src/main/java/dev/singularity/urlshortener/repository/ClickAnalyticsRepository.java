package dev.singularity.urlshortener.repository;

import dev.singularity.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, UUID> {

    /** Bonus over the original: an endpoint to list clicks per short URL
     *  becomes a one-liner instead of another hand-written query. */
    List<ClickAnalytics> findByShortUrlOrderByTimestampDesc(String shortUrl);
}
