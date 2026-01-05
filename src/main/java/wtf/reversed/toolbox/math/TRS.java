package wtf.reversed.toolbox.math;

/**
 * Transformation record for translation, rotation, and scale.
 *
 * @param translation The translation vector.
 * @param rotation    The rotation quaternion.
 * @param scale       The scale vector.
 */
public record TRS(
    Vector3 translation,
    Quaternion rotation,
    Vector3 scale
) {
}
