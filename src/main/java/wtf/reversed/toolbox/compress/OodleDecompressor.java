package wtf.reversed.toolbox.compress;

import java.io.*;
import java.lang.foreign.*;
import java.nio.*;
import java.nio.file.*;

final class OodleDecompressor extends Decompressor {
    private final Arena arena;
    private final OodleFFM library;

    public OodleDecompressor(Path path) {
        this.arena = Arena.ofConfined();
        this.library = new OodleFFM(path, arena);
    }

    @Override
    public void decompress(ByteBuffer src, ByteBuffer dst) throws IOException {
        try (var arena = Arena.ofConfined()) {
            var srcSegment = arena.allocate(src.remaining()).copyFrom(MemorySegment.ofBuffer(src));
            var dstSegment = arena.allocate(dst.remaining());

            var result = library.OodleLZ_Decompress(
                srcSegment, srcSegment.byteSize(),
                dstSegment, dstSegment.byteSize(),
                1 /* OodleLZ_FuzzSafe_Yes */,
                1 /* OodleLZ_CheckCRC_Yes */,
                0 /* OodleLZ_Verbosity_None */,
                MemorySegment.NULL, 0,
                MemorySegment.NULL, MemorySegment.NULL,
                MemorySegment.NULL, 0,
                3 /* OodleLZ_Decode_ThreadPhaseAll */
            );

            if (result != dst.remaining()) {
                throw new IOException("Error decompressing data");
            }

            MemorySegment.ofBuffer(dst).copyFrom(dstSegment);
        }
    }

    @Override
    public void close() {
        arena.close();
    }
}
