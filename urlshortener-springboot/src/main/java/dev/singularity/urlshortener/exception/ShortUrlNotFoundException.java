package dev.singularity.urlshortener.exception;

/** Thrown on GET /{shortUrl} when no mapping exists. Mapped to HTTP 404. */
public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String shortUrl) {
        super("Short URL not found: " + shortUrl);
    }
}
