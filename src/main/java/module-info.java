module wtf.reversed.toolbox {
    requires static java.compiler;  // Required for WrapperGenerator
    requires static org.tukaani.xz; // Required for LZMADecompressor

    exports wtf.reversed.toolbox.collect;
    exports wtf.reversed.toolbox.compress;
    exports wtf.reversed.toolbox.hash;
    exports wtf.reversed.toolbox.io;
    exports wtf.reversed.toolbox.math;
    exports wtf.reversed.toolbox.type;
    exports wtf.reversed.toolbox.util;
}
