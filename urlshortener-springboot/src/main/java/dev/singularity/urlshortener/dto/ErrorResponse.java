package dev.singularity.urlshortener.dto;

/** Matches the original `{ "error": "..." }` shape used for every 4xx/5xx. */
public record ErrorResponse(String error) {
}
