package dev.singularity.urlshortener.repository;

import dev.singularity.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA derives the SQL for these two methods purely from their
 * *names* at startup — no @Query, no manual "SELECT short_url FROM urls
 * WHERE long_url = $1" like in the Node version. `findByLongUrl` becomes
 * `SELECT * FROM urls WHERE long_url = ?`, and so on.
 *
 * JpaRepository<UrlMapping, Long> already gives you save(), findById(),
 * findAll(), deleteById(), count(), etc. for free.
 */
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByLongUrl(String longUrl);

    Optional<UrlMapping> findByShortUrl(String shortUrl);

    boolean existsByShortUrl(String shortUrl);
}
