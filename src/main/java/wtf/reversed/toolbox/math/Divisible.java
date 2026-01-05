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
 *   <li>{@link #multiply(T)} - multiplication operation</li>
 *   <li>{@link #conjugate()} - conjugate</li>
 * </ul>
 *
 * @param <T> the concrete type implementing this interface
 */
public interface Divisible<T extends Divisible<T>> extends Vector<T> {
    /**
     * Multiplies this element by another element.
     * This operation need not be commutative.
     *
     * @param other the element to multiply by
     * @return the product of this and other
     */
    T multiply(T other);

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

    /**
     * Returns the conjugate of this element.
     *
     * <p>For complex numbers z = a + bi: conjugate(z) = a - bi
     * <br>For quaternions q = w + xi + yj + zk: conjugate(q) = w - xi - yj - zk
     *
     * <p>The conjugate satisfies: conjugate(xy) = conjugate(y) · conjugate(x)
     * (note the reversal of order for non-commutative algebras)
     *
     * @return the conjugate of this element
     */
    T conjugate();

    /**
     * Returns the multiplicative inverse of this element using the formula:
     * <pre>
     *     inverse(x) = conjugate(x) / normSquared(x)
     * </pre>
     * <p>
     * For a non-zero element x, x.multiply(x.inverse()) equals one().
     *
     * @return the multiplicative inverse
     * @throws ArithmeticException if this element has no inverse (i.e., norm is zero)
     */
    default T inverse() {
        return conjugate().divide(lengthSquared());
    }
}
