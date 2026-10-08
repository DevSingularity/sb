package dev.singularity.urlshortener.exception;

/** Thrown when the submitted longURL fails java.net.URI validation. Mapped
 *  to HTTP 400 by GlobalExceptionHandler. */
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
