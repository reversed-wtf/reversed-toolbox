package wtf.reversed.toolbox.math;

/**
 * Interface for vector spaces with basic arithmetic operations.
 *
 * @param <T> the type of the vector space
 */
public interface VectorSpace<T extends VectorSpace<T>> {

    /**
     * Adds another spatial object to this one.
     *
     * @param other the spatial object to add
     * @return the result of the addition
     */
    T add(T other);

    /**
     * Subtracts another spatial object from this one.
     *
     * @param other the spatial object to subtract
     * @return the result of the subtraction
     */
    default T subtract(T other) {
        return add(other.negate());
    }

    /**
     * Multiplies this spatial object by a scalar.
     *
     * @param scalar the scalar to multiply by
     * @return the result of the multiplication
     */
    T multiply(float scalar);

    /**
     * Divides this spatial object by a scalar.
     *
     * @param scalar the scalar to divide by
     * @return the result of the division
     */
    default T divide(float scalar) {
        return multiply(1.0f / scalar);
    }

    /**
     * Negates this spatial object.
     *
     * @return the negated spatial object
     */
    default T negate() {
        return multiply(-1.0f);
    }

}
