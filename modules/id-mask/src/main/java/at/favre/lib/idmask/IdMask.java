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
 * IdMask should be thread-safe.
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
     * Simple interface that defines how a java types is converted to a byte array and vice versa.
     *
     * @param <T> to convert to
     */
    interface TypeConverter<T> {
        /**
         * Converts given byte array to a type´.
         *
         * @param raw to convert
         * @return the type representation of given byte array
         */
        T convertFromBytes(byte[] raw);

        /**
         * Converts given java type to it's byte array representation
         *
         * @param typed to convert
         * @return the byte array representation of given java type
         */
        byte[] convertToBytes(T typed);
    }

    /**
     * Base implementation
     */
    abstract class BaseIdMask<T> implements IdMask<T> {
        private final IdMaskEngine engine;
        private final Config config;
        private final TypeConverter<T> typeConverter;

        BaseIdMask(IdMaskEngine engine, TypeConverter<T> typeConverter, Config config) {
            this.engine = engine;
            this.typeConverter = typeConverter;
            this.config = config;
        }

        @Override
        public String mask(T id) {
            final byte[] idAsBytes = typeConverter.convertToBytes(id);

            String encoded;
            if (config.enableCache() && !config.randomizedIds()) {
                if ((encoded = config.cacheImpl().getEncoded(idAsBytes)) != null) {
                    return encoded;
                }
            }

            encoded = engine.mask(idAsBytes).toString();

            if (config.enableCache()) {
                config.cacheImpl().cache(idAsBytes, encoded);
            }

            return encoded;
        }

        @Override
        public T unmask(String encoded) {
            byte[] raw;
            if (config.enableCache() && !config.randomizedIds()) {
                if ((raw = config.cacheImpl().getBytes(encoded)) != null) {
                    return typeConverter.convertFromBytes(Bytes.wrap(raw).copy().array());
                }
            }

            raw = engine.unmask(encoded);

            if (config.enableCache()) {
                config.cacheImpl().cache(raw, encoded);
            }

            return typeConverter.convertFromBytes(raw);
        }
    }

    /**
     * Implementation which handles int type ids (32 bit integers)
     */
    final class IntIdMask extends BaseIdMask<Integer> {

        IntIdMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_4_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new IntTypeConverter(),
                    config);
        }

        private static final class IntTypeConverter implements TypeConverter<Integer> {
            @Override
            public byte[] convertToBytes(Integer typed) {
                return Bytes.from(typed).array();
            }

            @Override
            public Integer convertFromBytes(byte[] raw) {
                return Bytes.wrap(raw).toInt();
            }
        }
    }

    /**
     * Implementation which handles long type ids (64 bit integers)
     */
    final class LongIdMask extends BaseIdMask<Long> {

        LongIdMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new LongTypeConverter(),
                    config);
        }

        private static final class LongTypeConverter implements TypeConverter<Long> {
            @Override
            public byte[] convertToBytes(Long typed) {
                return Bytes.from(typed).array();
            }

            @Override
            public Long convertFromBytes(byte[] raw) {
                return Bytes.wrap(raw).toLong();
            }
        }
    }

    /**
     * Implementation which handles two long ids (2x 64 bit ids)
     */
    final class LongIdTupleMask extends BaseIdMask<LongTuple> {

        LongIdTupleMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new LongIdTupleConverter(),
                    config);
        }

        private static final class LongIdTupleConverter implements TypeConverter<LongTuple> {
            @Override
            public byte[] convertToBytes(LongTuple id) {
                return Bytes.from(id.getNum1(), id.getNum2()).array();
            }

            @Override
            public LongTuple convertFromBytes(byte[] out) {
                return new LongTuple(Bytes.wrap(out).longAt(0), Bytes.wrap(out).longAt(8));
            }
        }
    }

    /**
     * Implementation which handles uuids (Universally unique identifier).
     * <p>
     * You can use {@link UUID#fromString(String)} to parse a string representation
     * to the typed version.
     */
    final class UuidMask extends BaseIdMask<UUID> {

        UuidMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new UuidConverter(),
                    config);
        }

        private static final class UuidConverter implements TypeConverter<UUID> {
            @Override
            public byte[] convertToBytes(UUID id) {
                return Bytes.from(id).array();
            }

            @Override
            public UUID convertFromBytes(byte[] out) {
                return Bytes.wrap(out).toUUID();
            }
        }
    }

    /**
     * Implementation which handles a generic 128 bit integer
     * (or other 16 byte long array)
     */
    final class ByteArray128bitMask extends BaseIdMask<byte[]> {

        ByteArray128bitMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new ByteArrayConverter(),
                    config);
        }

        private static final class ByteArrayConverter implements TypeConverter<byte[]> {
            @Override
            public byte[] convertToBytes(byte[] id) {
                return id;
            }

            @Override
            public byte[] convertFromBytes(byte[] out) {
                return out;
            }
        }
    }

    /**
     * Implementation which handles a big integer up to 15 byte two complements representation
     */
    final class BigIntegerIdMask extends BaseIdMask<BigInteger> {
        private static final int SUPPORTED_LENGTH = 15;

        BigIntegerIdMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new BigIntegerConverter(),
                    config);
        }

        private static final class BigIntegerConverter implements TypeConverter<BigInteger> {
            @Override
            public byte[] convertToBytes(BigInteger id) {
                Bytes bytes = Bytes.from(id);
                if (bytes.length() > SUPPORTED_LENGTH) {
                    throw new IllegalArgumentException("biginteger only supports up to " + SUPPORTED_LENGTH + " byte two-complements representation");
                }

                ByteBuffer bb = ByteBuffer.allocate(SUPPORTED_LENGTH + 1);
                bb.put((byte) bytes.length());
                bb.put(bytes.resize(SUPPORTED_LENGTH).array());
                return bb.array();
            }

            @Override
            public BigInteger convertFromBytes(byte[] out) {
                ByteBuffer bb = ByteBuffer.wrap(out);
                int length = bb.get();
                byte[] number = new byte[bb.remaining()];
                bb.get(number);
                return Bytes.wrap(number).resize(length).toBigInteger();
            }
        }
    }

    /**
     * Implementation which handles a generic 256 bit integer
     * (or other 32 byte long array)
     */
    final class ByteArray256bitMask extends BaseIdMask<byte[]> {

        ByteArray256bitMask(Config config) {
            super(new IdMaskEngine.AesSivEngine(config.keyManager(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_32_BYTE,
                            config.encoding(), config.randomizedIds(), config.secureRandom(), config.securityProvider()),
                    new ByteArray128bitMask.ByteArrayConverter(),
                    config);
        }
    }

    final class CustomIdMaskEngine<T> extends BaseIdMask<T> {
        CustomIdMaskEngine(IdMaskEngine idMaskEngine, TypeConverter<T> typeConverter, Config config) {
            super(idMaskEngine, typeConverter, config);
        }
    }
}
