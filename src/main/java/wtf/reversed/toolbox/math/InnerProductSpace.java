package wtf.reversed.toolbox.math;

/**
 * Interface for vector spaces with an inner (dot) product.
 *
 * @param <T> the type of the vector space
 */
public interface InnerProductSpace<T extends InnerProductSpace<T>> extends VectorSpace<T> {

    /**
     * A small epsilon value for floating-point comparisons.
     */
    float EPSILON = 1e-4f;

    /**
     * Computes the dot product of this vector with another.
     *
     * @param other the other vector
     * @return the dot product
     */
    float dot(T other);

    /**
     * Computes the squared length of this vector.
     *
     * @return the squared length
     */
    @SuppressWarnings("unchecked")
    default float lengthSquared() {
        return dot((T) this);
    }

    /**
     * Computes the length of this vector.
     *
     * @return the length
     */
    default float length() {
        return FloatMath.sqrt(lengthSquared());
    }

    /**
     * Returns a normalized version of this vector (length of 1).
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
    default boolean isNormalized() {
        return Math.abs(lengthSquared() - 1.0f) < EPSILON;
    }

}
