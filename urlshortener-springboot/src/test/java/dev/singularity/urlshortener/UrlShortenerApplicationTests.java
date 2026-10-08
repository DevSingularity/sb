package dev.singularity.urlshortener;

import com.redis.testcontainers.RedisContainer;
import dev.singularity.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is the modern equivalent of manually starting `docker compose up`
 * before running tests. Testcontainers spins up throwaway Postgres + Redis
 * Docker containers just for this test class, wires Spring's DataSource and
 * RedisConnectionFactory to point at them via @ServiceConnection, and tears
 * them down afterwards. No shared, stateful "test database" to accidentally
 * pollute.
 *
 * Requires a working Docker daemon on the machine running the tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class UrlShortenerApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Test
    void contextLoads() {
        // If the Spring context fails to start (bad config, missing bean,
        // failed Flyway migration...) this test fails with the real cause
        // in the stack trace — the single most useful test in any Spring
        // Boot project.
    }

    @Test
    void shorteningTheSameLongUrlTwiceReturnsTheSameShortCode() {
        String longUrl = "https://www.anthropic.com/some/very/long/path?query=1";

        var first = urlShortenerService.createOrGetShortUrl(longUrl);
        var second = urlShortenerService.createOrGetShortUrl(longUrl);

        assertThat(first.newlyCreated()).isTrue();
        assertThat(second.newlyCreated()).isFalse();
        assertThat(second.shortUrl()).isEqualTo(first.shortUrl());
    }

    @Test
    void resolvingAKnownShortUrlReturnsTheOriginalLongUrl() {
        String longUrl = "https://example.com/resolve-me";
        var created = urlShortenerService.createOrGetShortUrl(longUrl);

        String resolved = urlShortenerService.findLongUrlByShortUrl(created.shortUrl());

        assertThat(resolved).isEqualTo(longUrl);
    }
}
