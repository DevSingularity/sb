package dev.singularity.urlshortener.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Recreates the two Redis cache "namespaces" from the original code:
 *   - redisClient.setEx(`long:${longURL}`,  ...)   -> cache name "urls-by-long"
 *   - redisClient.setEx(`short:${shortURL}`, ...)  -> cache name "urls-by-short"
 * with the same 24h TTL (CACHE_TTL_SECONDS = 86400 in urlService.js).
 *
 * Both cached values here are plain Strings (a short code or a long URL),
 * so a StringRedisSerializer is used for both keys AND values — this is
 * deliberately simpler than wrapping every value in a JSON envelope.
 *
 * Gotcha worth flagging: {@code RedisCacheManagerBuilderCustomizer} lived in
 * {@code org.springframework.data.redis.cache} on Spring Boot 3. On Boot 4
 * it moved to {@code org.springframework.boot.cache.autoconfigure} — the
 * class itself is unchanged, only the package, but it's exactly the kind
 * of silent break that makes copy-pasting from an older tutorial fail with
 * a bare "cannot find symbol" and no further hint.
 *
 * Worth knowing for later: if you cache actual objects (e.g. a whole
 * UrlMapping), you'd reach for Spring Data Redis's JSON serializer instead
 * — GenericJacksonJsonRedisSerializer on the Jackson-3 stack this project
 * uses (Spring Boot 4 default), or the older GenericJackson2JsonRedisSerializer
 * name if a project is still on the Jackson-2 compatibility path. Since
 * Jackson's own module split (see README's Jackson 3 section) moved fast
 * around the Spring Boot 4 release, always check the exact class name
 * against the spring-data-redis version actually on your classpath rather
 * than copying it from an older tutorial.
 */
@Configuration
public class CacheConfig {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return baseConfig();
    }

    /** Gives each of the two caches an explicit, named config (visible if
     *  you run `redis-cli KEYS '*'` while the app is running). */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisCacheConfiguration config = baseConfig();
        return builder -> builder
                .withCacheConfiguration("urls-by-long", config)
                .withCacheConfiguration("urls-by-short", config);
    }

    private RedisCacheConfiguration baseConfig() {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CACHE_TTL)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer));
    }
}
