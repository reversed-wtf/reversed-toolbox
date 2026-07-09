package wtf.reversed.toolbox.compress;

/**
 * Unchecked exception thrown by {@link Compressor} implementations when compression cannot complete. Typical causes
 * include small output buffer, or a failure reported by an underlying native library.
 */
public final class CompressorException extends RuntimeException {
    CompressorException(String message) {
        super(message);
    }

    CompressorException(String message, Throwable cause) {
        super(message, cause);
    }
}
