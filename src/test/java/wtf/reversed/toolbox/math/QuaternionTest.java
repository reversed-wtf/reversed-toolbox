package wtf.reversed.toolbox.math;

import nl.jqno.equalsverifier.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class QuaternionTest {
    @Test
    void testEqualsAndHashCode() {
        EqualsVerifier.forClass(Quaternion.class).verify();
    }

    @Test
    void testConsistency() {
        float x = Angle.DEGREES.toRadians(20);
        float y = Angle.DEGREES.toRadians(40);
        float z = Angle.DEGREES.toRadians(60);
        Vector3 rot = new Vector3(x, y, z);

        Quaternion qx = Quaternion.fromAxisAngle(Vector3.X, x, Angle.RADIANS);
        Quaternion qy = Quaternion.fromAxisAngle(Vector3.Y, y, Angle.RADIANS);
        Quaternion qz = Quaternion.fromAxisAngle(Vector3.Z, z, Angle.RADIANS);

        Quaternion eulerXYZ = Quaternion.fromEuler(rot, Angle.RADIANS, Order.XYZ);
        Quaternion eulerXZY = Quaternion.fromEuler(rot, Angle.RADIANS, Order.XZY);
        Quaternion eulerYXZ = Quaternion.fromEuler(rot, Angle.RADIANS, Order.YXZ);
        Quaternion eulerYZX = Quaternion.fromEuler(rot, Angle.RADIANS, Order.YZX);
        Quaternion eulerZXY = Quaternion.fromEuler(rot, Angle.RADIANS, Order.ZXY);
        Quaternion eulerZYX = Quaternion.fromEuler(rot, Angle.RADIANS, Order.ZYX);

        Quaternion quatXYZ = qx.multiply(qy).multiply(qz);
        Quaternion quatXZY = qx.multiply(qz).multiply(qy);
        Quaternion quatYXZ = qy.multiply(qx).multiply(qz);
        Quaternion quatYZX = qy.multiply(qz).multiply(qx);
        Quaternion quatZXY = qz.multiply(qx).multiply(qy);
        Quaternion quatZYX = qz.multiply(qy).multiply(qx);

        assertThat(eulerXYZ.subtract(quatXYZ).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(eulerXZY.subtract(quatXZY).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(eulerYXZ.subtract(quatYXZ).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(eulerYZX.subtract(quatYZX).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(eulerZXY.subtract(quatZXY).lengthSquared()).isLessThan(Linear.EPSILON);
        assertThat(eulerZYX.subtract(quatZYX).lengthSquared()).isLessThan(Linear.EPSILON);
    }
}
