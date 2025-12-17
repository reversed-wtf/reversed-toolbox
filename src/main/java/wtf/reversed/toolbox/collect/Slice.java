package wtf.reversed.toolbox.collect;

import java.nio.*;

public interface Slice {
    int length();

    Buffer asBuffer();
}
