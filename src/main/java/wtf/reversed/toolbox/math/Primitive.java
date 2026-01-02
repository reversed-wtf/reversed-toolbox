package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

/**
 * Primitive represents a mathematical primitive with a fixed number of components.
 * <p>
 * These can be written to and read from buffers.
 */
public interface Primitive {

    /**
     * Copies the components of this primitive into the given mutable float collection.
     *
     * @param floats the mutable float collection to copy into
     * @param offset the offset within the collection to start copying into
     */
    void copyTo(Floats.Mutable floats, int offset);

    /**
     * Copies the components of this primitive into the given buffer.
     *
     * @param floats the buffer to copy into
     */
    void copyTo(FloatBuffer floats);

}
