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
public interface Matrix<T extends Matrix<T>> extends Divisible<T>, Linear<T> {

    /**
     * Returns the transpose of this matrix.
     *
     * @return the transposed matrix
     */
    T transpose();

    /**
     * Computes the determinant of this matrix.
     *
     * @return the determinant
     */
    float determinant();

    /**
     * Gets the component at the given row and column.
     * <p>
     * Note: This isn't really a space related property
     *
     * @param row    The index of the row to get.
     * @param column The index of the column to get.
     */
    float get(int row, int column);

}
