package wtf.reversed.toolbox.compress;

/**
 * Unchecked exception thrown by {@link Decompressor} implementations when decompression cannot complete. Typical causes
 * include malformed compressed input, an output size that does not match the destination buffer, or a failure reported
 * by an underlying native library.
 */
public final class DecompressorException extends RuntimeException {
    DecompressorException(String message) {
        super(message);
    }

    DecompressorException(String message, Throwable cause) {
        super(message, cause);
    }
}
