package wtf.reversed.toolbox.hash;

/**
 * A description of a CRC algorithm
 *
 * @param width  The width of the polynomial
 * @param poly   The polynomial itself
 * @param init   The initial value
 * @param refIn  If the input bits are reflected
 * @param refOut If the output bits are reflected
 * @param xorOut What value to xor the output with
 */
public record CRCAlgorithm(
    int width,
    long poly,
    long init,
    boolean refIn,
    boolean refOut,
    long xorOut
) {
}
