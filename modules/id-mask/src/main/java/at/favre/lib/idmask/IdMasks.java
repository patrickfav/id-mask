package at.favre.lib.idmask;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Factory and main API of the library. Use to create new IdMask instances for various types.
 */
@SuppressWarnings("WeakerAccess")
public final class IdMasks {

    private IdMasks() {
    }

    /**
     * Create new id mask for masking 64 bit integers.
     *
     * @param config to adjust settings
     * @return new instance
     */
    public static IdMask<Long> forLongIds(Config config) {
        return new IdMask.LongIdMask(config);
    }

    /**
     * Create new id mask for masking a tuple of 2 x 64 bit integers.
     *
     * @param config to adjust settings
     * @return new instance
     */
    public static IdMask<LongTuple> forLongTuples(Config config) {
        return new IdMask.LongIdTupleMask(config);
    }

    /**
     * Create new id mask for masking a UUIDs.
     * <p>
     * (see {@link UUID#fromString(String)} for converting from string representation.
     *
     * @param config to adjust settings
     * @return new instance
     */
    public static IdMask<UUID> forUuids(Config config) {
        return new IdMask.UuidMask(config);
    }

    /**
     * Create new id mask for 128 bit ids represented as byte array.
     *
     * @param config to adjust settings
     * @return new instance
     */
    public static IdMask<byte[]> for128bitNumbers(Config config) {
        return new IdMask.ByteArray128bitMask(config);
    }

    /**
     * Create new id mask for 128 bit ids represented as byte array.
     *
     * @param config to adjust settings
     * @return new instance
     */
    public static IdMask<BigInteger> forBigInteger(Config config) {
        return new IdMask.BigIntegerIdMask(config);
    }

    public static IdMask<Long> forSiv(Config config) {
        return new IdMask.AesSivMask(config);
    }
}
