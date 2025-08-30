module wtf.reversed.toolbox {
    // Required for LZMADecompressor
    requires static org.tukaani.xz;

    exports wtf.reversed.toolbox.compression;
    exports wtf.reversed.toolbox.hashing;
    exports wtf.reversed.toolbox.io;
    exports wtf.reversed.toolbox.type;
    exports wtf.reversed.toolbox.util;
}