package dev.singularity.urlshortener.controller;

import dev.singularity.urlshortener.dto.ShortenRequest;
import dev.singularity.urlshortener.dto.ShortenResponse;
import dev.singularity.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Same two endpoints as index.js, same paths, same JSON shapes:
 *   POST /api/v1/data   { longURL } -> 201 { shortURL } | 400 { error }
 *   GET  /{shortUrl}     -> 302 redirect | 404 { error }
 *
 * The static frontend (served from src/main/resources/static, exactly as
 * it was in the Node app's /public folder) talks to this controller
 * without any changes.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @Value("${app.base-url:http://localhost:9000}")
    private String baseUrl;

    @PostMapping("/api/v1/data")
    @Operation(summary = "Shorten a URL (idempotent per longURL)")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        UrlShortenerService.ShortenResult result = urlShortenerService.createOrGetShortUrl(request.longUrl());
        ShortenResponse body = new ShortenResponse(baseUrl + "/" + result.shortUrl());

        // Mirrors the original: 200 if the mapping already existed
        // (idempotency check hit), 201 if a new one was just created.
        HttpStatus status = result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(body);
    }

    /**
     * The path variable is constrained to exactly 8 lowercase hex chars —
     * which is *always* what HashUtils produces (both the sha256 slice and
     * the random-4-bytes collision fallback are 8 hex chars). This isn't
     * just validation: without it, this catch-all "/{shortUrl}" pattern
     * would also match "/index.html" and "/img.jpg" and — because Spring
     * MVC's @RequestMapping handlers are consulted before the static
     * resource handler — silently shadow the static frontend with a 404
     * JSON error. The original Express app didn't have this problem
     * because `express.static(...)` middleware ran *before* the
     * `app.get("/:shortURL", ...)` route was ever reached.
     */
    @GetMapping("/{shortUrl:[0-9a-f]{8}}")
    @Operation(summary = "Resolve a short code and redirect, logging a click")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request) {
        String longUrl = urlShortenerService.resolveAndRecordClick(shortUrl, request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", longUrl)
                .build();
    }
}
