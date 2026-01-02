package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;
import wtf.reversed.toolbox.util.*;

import java.nio.*;

/**
 * Primitive represents a mathematical primitive with a fixed number of components.
 * <p>
 * These can be written to and read from buffers.
 */
public interface Primitive {

    /**
     * Returns the number of components in this primitive.
     *
     * @return the number of components
     */
    int componentCount();

    /**
     * Copies the components of this primitive into the given mutable float collection.
     *
     * @param floats the mutable float collection to copy into
     * @param offset the offset within the collection to start copying into
     */
    default void toSlice(Floats.Mutable floats, int offset) {
        Check.fromIndexSize(offset, componentCount(), floats.length());
        toSliceUnsafe(floats, offset);
    }

    /**
     * Copies the components of this primitive into the given mutable float collection without bounds checking.
     *
     * @param floats the mutable float collection to copy into
     * @param offset the offset within the collection to start copying into
     */
    void toSliceUnsafe(Floats.Mutable floats, int offset);

    /**
     * Copies the components of this primitive into the given buffer.
     *
     * @param floats the buffer to copy into
     */
    default void toBuffer(FloatBuffer floats) {
        Check.argument(floats.remaining() >= componentCount(), "Not enough space in buffer");
        toBufferUnsafe(floats);
    }

    /**
     * Copies the components of this primitive into the given buffer without bounds checking.
     *
     * @param floats the buffer to copy into
     */
    void toBufferUnsafe(FloatBuffer floats);

}
