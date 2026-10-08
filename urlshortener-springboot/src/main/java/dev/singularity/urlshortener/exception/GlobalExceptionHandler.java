package dev.singularity.urlshortener.exception;

import dev.singularity.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralises every try/catch -> res.status(xxx).json({error}) block that
 * was scattered across index.js into one place. Any controller method that
 * throws one of these exceptions gets converted into the matching HTTP
 * response automatically — the controller method bodies stay focused on
 * the happy path.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ShortUrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    /** Fired automatically when @Valid on a @RequestBody fails, e.g. a
     *  missing/blank longURL — equivalent of the `if (!longURL)` check. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("No URL given");
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    /** Catch-all safety net — never leak a stack trace to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Internal Server Error"));
    }
}
