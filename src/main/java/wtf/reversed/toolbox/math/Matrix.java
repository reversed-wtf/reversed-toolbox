package wtf.reversed.toolbox.math;

/**
 * A matrix algebra supporting matrix operations.
 * <p>
 * This interface extends linear spaces with matrix-specific operations including
 * matrix multiplication, transpose, determinant, and inverse. Unlike vectors,
 * matrices support non-commutative multiplication.
 * <p>
 * Matrix multiplication must be associative: (AB)C = A(BC)
 *
 * @param <T> the concrete type implementing this interface
 */
public interface Matrix<T extends Matrix<T>> extends Linear<T> {
}
