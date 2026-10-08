package dev.singularity.urlshortener.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Direct port of:
 *   function generateHash(longURL) {
 *     return crypto.createHash("sha256").update(longURL).digest("hex").slice(0, 8);
 *   }
 *
 * java.security.MessageDigest is the JDK's built-in equivalent of Node's
 * `crypto` module — no extra dependency needed. HexFormat (added in Java 17)
 * replaces the manual byte-to-hex-string loops you'll see in older Java
 * tutorials.
 */
public final class HashUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private HashUtils() {
    }

    public static String sha256First8(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every standard JDK,
            // so this branch is unreachable in practice.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Equivalent of `crypto.randomBytes(4).toString("hex")` — used only as
     *  a collision fallback, same as the original. */
    public static String randomHex4Bytes() {
        byte[] bytes = new byte[4];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
