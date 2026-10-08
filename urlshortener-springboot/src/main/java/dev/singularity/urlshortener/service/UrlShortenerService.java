package dev.singularity.urlshortener.service;

import dev.singularity.urlshortener.entity.ClickAnalytics;
import dev.singularity.urlshortener.entity.UrlMapping;
import dev.singularity.urlshortener.exception.InvalidUrlException;
import dev.singularity.urlshortener.exception.ShortUrlNotFoundException;
import dev.singularity.urlshortener.repository.ClickAnalyticsRepository;
import dev.singularity.urlshortener.repository.UrlMappingRepository;
import dev.singularity.urlshortener.util.HashUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Direct translation of urlService.js's four exported functions, plus the
 * validation + collision loop that lived inline in index.js's POST handler.
 *
 * Every public method here traces back to the original:
 *   getShortURLByLong  -> findShortUrlByLongUrl (delegates to UrlCacheService)
 *   getLongURLByShort  -> findLongUrlByShortUrl (delegates to UrlCacheService)
 *   saveURLMapping     -> folded into createOrGetShortUrl
 *   recordClickInDb    -> resolveAndRecordClick
 *
 * See UrlCacheService's Javadoc for *why* the @Cacheable/@CachePut-annotated
 * methods live on a separate bean rather than here (self-invocation +
 * Spring AOP proxies).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;
    private final ClickAnalyticsRepository clickAnalyticsRepository;
    private final UrlCacheService urlCacheService;

    /**
     * Equivalent of `new URL(longURL)` inside the try/catch in index.js.
     * java.net.URI is the JDK's URL-shaped value type; URL itself is
     * considered legacy for validation because its equals()/hashCode() do
     * a DNS lookup — see JDK-8221191 if you want the full story, a fun
     * rabbit hole for later.
     */
    private void validate(String longUrl) {
        try {
            URI uri = new URI(longUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidUrlException("Invalid URL");
            }
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL");
        }
    }

    /** Cache-aside read (Redis first, DB on a miss) — see UrlCacheService. */
    public String findShortUrlByLongUrl(String longUrl) {
        return urlCacheService.getShortUrlByLongUrl(longUrl);
    }

    public String findLongUrlByShortUrl(String shortUrl) {
        return urlCacheService.getLongUrlByShortUrl(shortUrl);
    }

    /**
     * Full create-or-fetch flow, matching the POST /api/v1/data handler:
     *   1. idempotency check (return existing short URL if this long URL
     *      was already shortened)
     *   2. hash the long URL, re-roll with a random suffix on collision
     *   3. persist + warm both cache entries
     */
    @Transactional
    public ShortenResult createOrGetShortUrl(String longUrl) {
        validate(longUrl);

        String existing = urlCacheService.getShortUrlByLongUrl(longUrl);
        if (existing != null) {
            return new ShortenResult(existing, false);
        }

        String shortUrl = HashUtils.sha256First8(longUrl);
        // Collision handling: identical to the while loop in index.js —
        // keep re-rolling with a random 4-byte hex suffix until the short
        // code is free, or already points at this exact long URL. This
        // check must go straight to the repository (not the cache) since
        // a code that was never cached could still exist in Postgres.
        String collidingLongUrl = urlMappingRepository.findByShortUrl(shortUrl)
                .map(UrlMapping::getLongUrl)
                .orElse(null);
        while (collidingLongUrl != null && !collidingLongUrl.equals(longUrl)) {
            shortUrl = HashUtils.randomHex4Bytes();
            collidingLongUrl = urlMappingRepository.findByShortUrl(shortUrl)
                    .map(UrlMapping::getLongUrl)
                    .orElse(null);
        }

        urlMappingRepository.save(new UrlMapping(longUrl, shortUrl));
        urlCacheService.putShortUrl(longUrl, shortUrl);
        urlCacheService.putLongUrl(shortUrl, longUrl);

        log.info("Created short URL {} -> {}", shortUrl, longUrl);
        return new ShortenResult(shortUrl, true);
    }

    /** Small carrier so the controller can pick 200 vs 201 without a
     *  second cache/DB lookup. */
    public record ShortenResult(String shortUrl, boolean newlyCreated) {
    }

    /**
     * Resolves a short URL for redirecting AND records the click, exactly
     * like the GET /:shortURL handler which awaits recordClickInDb() before
     * issuing the 302. (See README for a note on making this asynchronous.)
     */
    @Transactional
    public String resolveAndRecordClick(String shortUrl, HttpServletRequest request) {
        String longUrl = urlCacheService.getLongUrlByShortUrl(shortUrl);
        if (longUrl == null) {
            throw new ShortUrlNotFoundException(shortUrl);
        }

        ClickAnalytics click = ClickAnalytics.builder()
                .id(UUID.randomUUID())
                .shortUrl(shortUrl)
                .longUrl(longUrl)
                .timestamp(LocalDateTime.now())
                .ip(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .referer(request.getHeader("Referer"))
                .build();
        clickAnalyticsRepository.save(click);

        return longUrl;
    }
}
