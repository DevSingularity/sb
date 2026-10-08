package dev.singularity.urlshortener.service;

import dev.singularity.urlshortener.entity.UrlMapping;
import dev.singularity.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Why this exists as its OWN Spring bean instead of just being more methods
 * on UrlShortenerService — this is one of the most-tripped-over Spring
 * gotchas, worth understanding properly rather than papering over:
 *
 * {@code @Cacheable}/{@code @CachePut}/{@code @Transactional} all work via a
 * runtime PROXY that Spring wraps around your bean. The proxy is what
 * intercepts the method call to check the cache / open a transaction before
 * your actual code runs. That interception only happens when the call comes
 * from OUTSIDE the bean, through the proxy.
 *
 * If UrlShortenerService.createOrGetShortUrl() called
 * `this.findShortUrlByLongUrl(...)` directly (a "self-invocation"), that's a
 * plain Java method call on `this` — it never goes through the proxy, so
 * the @Cacheable annotation is silently ignored. No error, no warning, it
 * just quietly never caches anything. This bit enough real production
 * codebases that it's practically a rite of passage.
 *
 * By putting the cached methods on a separate bean and injecting it,
 * every call from UrlShortenerService to UrlCacheService crosses a real
 * bean boundary and goes through the proxy correctly.
 */
@Service
@RequiredArgsConstructor
public class UrlCacheService {

    private final UrlMappingRepository urlMappingRepository;

    @Cacheable(cacheNames = "urls-by-long", key = "#longUrl", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getShortUrlByLongUrl(String longUrl) {
        return urlMappingRepository.findByLongUrl(longUrl)
                .map(UrlMapping::getShortUrl)
                .orElse(null);
    }

    @Cacheable(cacheNames = "urls-by-short", key = "#shortUrl", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getLongUrlByShortUrl(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl)
                .map(UrlMapping::getLongUrl)
                .orElse(null);
    }

    @CachePut(cacheNames = "urls-by-long", key = "#longUrl")
    public String putShortUrl(String longUrl, String shortUrl) {
        return shortUrl;
    }

    @CachePut(cacheNames = "urls-by-short", key = "#shortUrl")
    public String putLongUrl(String shortUrl, String longUrl) {
        return longUrl;
    }
}
