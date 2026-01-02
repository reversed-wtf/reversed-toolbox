package wtf.reversed.toolbox.math;

/**
 * A linear space supporting addition and scalar multiplication.
 * <p>
 * This interface represents a vector space in the mathematical sense,
 * defining the fundamental operations required for linear combinations:
 * addition of elements and multiplication by scalars.
 * <p>
 * Implementations must satisfy the vector space axioms:
 * <ul>
 *   <li>Additive Associativity: {@code (x + y) + z = x + (y + z)}</li>
 *   <li>Additive Commutativity: {@code x + y = y + x}</li>
 *   <li>Additive Identity: {@code x + 0 = x}</li>
 *   <li>Additive Inverse: {@code x + (-x) = 0}</li>
 *   <li>Scalar Multiplicative Associativity: {@code a(bx) = (ab)x}</li>
 *   <li>Scalar Multiplicative Identity: {@code 1x = x}</li>
 *   <li>Scalar Distributivity: {@code a(x + y) = ax + ay}</li>
 *   <li>Vector Distributivity: {@code (a + b)x = ax + bx}</li>
 * </ul>
 *
 * @param <T> the concrete type implementing this interface
 */
public interface Linear<T extends Linear<T>> {

    /**
     * Adds another element to this one.
     *
     * @param other the element to add
     * @return the sum of this and other
     */
    T add(T other);

    /**
     * Subtracts another element from this one.
     *
     * @param other the element to subtract
     * @return the difference of this and other
     */
    default T subtract(T other) {
        return add(other.negate());
    }

    /**
     * Multiplies this element by a scalar.
     *
     * @param scalar the scalar multiplier
     * @return the scaled element
     */
    T multiply(float scalar);

    /**
     * Divides this element by a scalar.
     *
     * @param scalar the scalar divisor
     * @return the scaled element
     */
    default T divide(float scalar) {
        return multiply(1.0f / scalar);
    }

    /**
     * Negates this element.
     *
     * @return the additive inverse of this element
     */
    default T negate() {
        return multiply(-1.0f);
    }

}
