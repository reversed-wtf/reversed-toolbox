package wtf.reversed.toolbox.math;

import wtf.reversed.toolbox.collect.*;

import java.nio.*;

public interface Primitive {

    /**
     * Gets the component at the given index.
     *
     * @param index the index of the component to get
     */
    float get(int index);

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
