package wtf.reversed.toolbox.math;

/**
 * Specifies the order of rotation for Euler angles.
 */
public enum Order {
    /**
     * First around the X axis, then around the Y axis, then around the Z axis.
     */
    XYZ,
    /**
     * First around the X axis, then around the Z axis, then around the Y axis.
     */
    XZY,
    /**
     * First around the Y axis, then around the X axis, then around the Z axis.
     */
    YXZ,
    /**
     * First around the Y axis, then around the Z axis, then around the X axis.
     */
    YZX,
    /**
     * First around the Z axis, then around the X axis, then around the Y axis.
     */
    ZXY,
    /**
     * First around the Z axis, then around the Y axis, then around the X axis.
     */
    ZYX
}
