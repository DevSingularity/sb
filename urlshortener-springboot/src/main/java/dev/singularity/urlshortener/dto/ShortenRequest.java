package dev.singularity.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * A Java `record` is an immutable data carrier: the class body below
 * generates a constructor, getters (longUrl(), not getLongUrl()), equals(),
 * hashCode() and toString() automatically. It's the closest thing Java has
 * to a plain `{ longURL }` object literal in JS, but typed and immutable.
 *
 * Field name is `longUrl` (Java camelCase convention) but Jackson 3 will
 * still bind the incoming JSON key `longURL` from the *existing* frontend
 * because of the @JsonProperty override below — the static HTML page in
 * public/index.html was left completely untouched, so it must keep working
 * exactly as before.
 */
public record ShortenRequest(
        @JsonProperty("longURL")
        @NotBlank(message = "longURL must not be blank")
        String longUrl
) {
}
