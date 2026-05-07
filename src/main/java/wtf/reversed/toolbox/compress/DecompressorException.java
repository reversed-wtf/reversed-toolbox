package wtf.reversed.toolbox.compress;

public final class DecompressorException extends RuntimeException {
    public DecompressorException(String message) {
        super(message);
    }

    public DecompressorException(String message, Throwable cause) {
        super(message, cause);
    }
}
