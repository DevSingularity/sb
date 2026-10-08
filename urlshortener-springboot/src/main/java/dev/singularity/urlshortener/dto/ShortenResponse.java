package dev.singularity.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keeps the exact `{ "shortURL": "..." }` shape the existing frontend
 * (public/index.html) already parses via `data.shortURL`.
 */
public record ShortenResponse(
        @JsonProperty("shortURL") String shortUrl
) {
}
