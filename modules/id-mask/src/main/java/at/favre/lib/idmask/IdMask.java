package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Id mask is responsible for masking/encoding ids in a reversible way.
 * <p>
 * This is mainly used for database or other ids which are published to the public
 * and should be obfuscated as to make it harder to guess and understand related ids
 * (e.g in a sequence).
 * <p>
 * IdMask should be thread
 *
 * @param <T> type of the id
 */
public interface IdMask<T> {

    /**
     * Mask a given id.
     *
     * @param id to mask
     * @return encoded masked id
     * @throws IllegalArgumentException if basic parameter validation fails
     */
    String mask(T id);

    /**
     * Unmask id that was previously masked with {@link #mask(Object)}
     *
     * @param encoded to unmask
     * @return original id
     * @throws IdMaskSecurityException  if used secret key, authentication tag, or version identifiers are incorrect
     * @throws IllegalArgumentException if basic parameter validation fails
     */
    T unmask(String encoded);

    /**
     * Base implementation
     */
    abstract class BaseIdMask {
        private final IdMaskEngine engine;
        private final Config config;

        BaseIdMask(IdMaskEngine engine, Config config) {
            this.engine = engine;
            this.config = config;
        }

        String _encode(byte[] id) {
            String encoded;
            if (config.enableCache() && !config.randomizedIds()) {
                if ((encoded = config.cacheImpl().getEncoded(id)) != null) {
                    return encoded;
                }
            }

            encoded = engine.mask(id).toString();

            if (config.enableCache()) {
                config.cacheImpl().cache(id, encoded);
            }

            return encoded;
        }

        byte[] _decode(String encoded) {
            byte[] raw;
            if (config.enableCache() && !config.randomizedIds()) {
                if ((raw = config.cacheImpl().getBytes(encoded)) != null) {
                    return Bytes.wrap(raw).copy().array();
                }
            }

            raw = engine.unmask(encoded);

            if (config.enableCache()) {
                config.cacheImpl().cache(raw, encoded);
            }

            return raw;
        }
    }

    /**
     * Implementation which handles long type ids (64 bit integers)
     */
    final class LongIdMask extends BaseIdMask implements IdMask<Long> {

        LongIdMask(Config config) {
            super(new IdMaskEngine.EightByteEncryptionEngine(config.keyManager(), config.securityProvider(),
                    config.secureRandom(), config.encoding(), config.randomizedIds()), config);
        }

        @Override
        public String mask(Long id) {
            return _encode(Bytes.from(id).array());
        }

        @Override
        public Long unmask(String encoded) {
            byte[] out = _decode(encoded);
            return Bytes.wrap(out).toLong();
        }
    }

    /**
     * Implementation which handles two long ids (2x 64 bit ids)
     */
    final class LongIdTupleMask extends BaseIdMask implements IdMask<LongTuple> {

        LongIdTupleMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String mask(LongTuple id) {
            return _encode(Bytes.from(id.getNum1(), id.getNum2()).array());
        }

        @Override
        public LongTuple unmask(String encoded) {
            Bytes out = Bytes.wrap(_decode(encoded));
            return new LongTuple(out.longAt(0), out.longAt(8));
        }
    }

    /**
     * Implementation which handles uuids (Universally unique identifier).
     * <p>
     * You can use {@link UUID#fromString(String)} to parse a string representation
     * to the typed version.
     */
    final class UuidMask extends BaseIdMask implements IdMask<UUID> {

        UuidMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String mask(UUID id) {
            return _encode(Bytes.from(id).array());
        }

        @Override
        public UUID unmask(String encoded) {
            byte[] out = _decode(encoded);
            return Bytes.wrap(out).toUUID();
        }
    }

    /**
     * Implementation which handles a generic 128 bit integer
     * (or other 16 byte long array)
     */
    final class ByteArray128bitMask extends BaseIdMask implements IdMask<byte[]> {

        ByteArray128bitMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String mask(byte[] id) {
            return _encode(Bytes.from(id).array());
        }

        @Override
        public byte[] unmask(String encoded) {
            return _decode(encoded);
        }
    }

    /**
     * Implementation which handles a big integer up to 15 byte two complements representation
     */
    final class BigIntegerIdMask extends BaseIdMask implements IdMask<BigInteger> {
        private static final int SUPPORTED_LENGTH = 15;

        BigIntegerIdMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String mask(BigInteger id) {
            Bytes bytes = Bytes.from(id);
            if (bytes.length() > SUPPORTED_LENGTH) {
                throw new IllegalArgumentException("biginteger only support up to " + SUPPORTED_LENGTH + " byte two-complements representation");
            }

            ByteBuffer bb = ByteBuffer.allocate(SUPPORTED_LENGTH + 1);
            bb.put((byte) bytes.length());
            bb.put(bytes.resize(SUPPORTED_LENGTH).array());

            return _encode(bb.array());
        }

        @Override
        public BigInteger unmask(String encoded) {
            ByteBuffer bb = ByteBuffer.wrap(_decode(encoded));
            int length = bb.get();
            byte[] number = new byte[bb.remaining()];
            bb.get(number);
            return Bytes.wrap(number).resize(length).toBigInteger();
        }
    }

}
