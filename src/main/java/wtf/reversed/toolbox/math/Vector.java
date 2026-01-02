package wtf.reversed.toolbox.math;

/**
 * A vector space with an inner product, enabling geometric operations.
 * <p>
 * This interface extends linear spaces with an inner product (dot product),
 * which induces a norm (length) and enables geometric concepts like distance,
 * angles, and projections.
 * <p>
 * The inner product must satisfy:
 * <ul>
 *   <li>Symmetry: a · b = b · a</li>
 *   <li>Linearity: (ka) · b = k(a · b)</li>
 *   <li>Positive definiteness: a · a ≥ 0, with equality iff a = 0</li>
 * </ul>
 *
 * @param <T> the concrete type implementing this interface
 */
public interface Vector<T extends Vector<T>> extends Linear<T> {

    /**
     * Gets the component at the given index.
     * <p>
     * Note: This isn't really a space related property
     *
     * @param index the index of the component to get
     */
    float get(int index);

    /**
     * Computes the dot product (inner product) with another vector.
     *
     * @param other the other vector
     * @return the dot product
     */
    float dot(T other);

    /**
     * Computes the squared length (squared norm) of this vector.
     *
     * @return the squared length
     */
    @SuppressWarnings("unchecked")
    default float lengthSquared() {
        return dot((T) this);
    }

    /**
     * Computes the length (Euclidean norm) of this vector.
     *
     * @return the length
     */
    default float length() {
        return FloatMath.sqrt(lengthSquared());
    }

    /**
     * Returns a normalized (unit length) version of this vector.
     *
     * @return the normalized vector
     */
    default T normalize() {
        return divide(length());
    }

    /**
     * Checks if this vector is normalized (length of 1).
     *
     * @return true if normalized, false otherwise
     */
    default boolean isNormalized(float epsilon) {
        return Math.abs(lengthSquared() - 1.0f) < epsilon;
    }

}
