package dev.singularity.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point. {@code @SpringBootApplication} is shorthand for three
 * annotations you'll see referenced constantly in docs:
 *   - @Configuration      this class can also declare @Bean methods
 *   - @EnableAutoConfiguration  Spring Boot guesses sensible beans from
 *                          whatever's on the classpath (see Postgres driver
 *                          on classpath -> DataSource auto-configured, etc.)
 *   - @ComponentScan       scans this package and sub-packages for
 *                          @Component/@Service/@Repository/@RestController
 *
 * {@code @EnableCaching} turns on Spring's cache abstraction so the
 * {@code @Cacheable}/{@code @CachePut} annotations in UrlShortenerService
 * actually do something (backed by Redis - see CacheConfig).
 */
@SpringBootApplication
@EnableCaching
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
