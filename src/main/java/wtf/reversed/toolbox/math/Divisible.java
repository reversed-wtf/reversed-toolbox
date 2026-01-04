package wtf.reversed.toolbox.math;

/**
 * Represents a division algebra: an algebraic structure where elements can be
 * multiplied together and every non-zero element has a multiplicative inverse.
 *
 * <p>Examples include real numbers, complex numbers, quaternions, and matrices.
 * Division algebras need not be commutative (e.g., quaternions, matrices).
 *
 * <p>Implementations must provide:
 * <ul>
 *   <li>{@link #one()} - multiplicative identity</li>
 *   <li>{@link #multiply(T)} - multiplication operation</li>
 *   <li>{@link #inverse()} - multiplicative inverse</li>
 * </ul>
 *
 * @param <T> the concrete type implementing this interface
 */
public interface Divisible<T extends Divisible<T>> {
    /**
     * Returns the multiplicative identity element.
     *
     * @return the identity element
     */
    T one();

    /**
     * Multiplies this element by another.
     *
     * @param other The element to multiply by.
     * @return The product of this and other.
     */
    T multiply(T other);

    /**
     * Returns the multiplicative inverse of this element.
     * For a non-zero element x, {@code x.multiply(x.inverse())} equals {@code one()}.
     *
     * @return the multiplicative inverse
     * @throws ArithmeticException if this element has no inverse (e.g., zero)
     */
    T inverse();

    /**
     * Right division: computes this / other = this × other⁻¹.
     *
     * @param other the divisor
     * @return the quotient
     * @throws ArithmeticException if other has no inverse
     */
    default T divide(T other) {
        return multiply(other.inverse());
    }
}
