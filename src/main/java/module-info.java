module wtf.reversed.toolbox {
    // Required for LZMADecompressor
    requires static org.tukaani.xz;

    exports wtf.reversed.toolbox.compress;
    exports wtf.reversed.toolbox.hash;
    exports wtf.reversed.toolbox.io;
    exports wtf.reversed.toolbox.type;
    exports wtf.reversed.toolbox.util;
}