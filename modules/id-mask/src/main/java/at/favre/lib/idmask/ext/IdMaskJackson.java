package at.favre.lib.idmask.ext;

import at.favre.lib.idmask.IdMask;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Default implementations for Jackson serializers &amp; deserializers. The jackson dependency is optional so you have
 * to add them to maven yourself if you want to use this class. The following modules are required:
 * <ul>
 * <li>com.fasterxml.jackson.core:jackson-core</li>
 * <li>com.fasterxml.jackson.core:jackson-databind</li>
 * </ul>
 * <p>
 * Tested with version 2.9.8 at the time of writing (2019).
 * <p>
 * You may use this for transparently converting your model id to a json string representation.
 * <p>
 * Example:
 * <p>
 * Extend the respective serializer/deserializer and provide your IDMask instance (or use DI to inject).
 * It is important to provide a no-arg constructor:
 *
 * <pre>
 * public final class MyIdMaskLongSerializers {
 *      private MyIdMaskLongSerializers() {
 *      }
 *
 *      &#64;Inject private final byte[] key;
 *
 *      public static final class Serializer extends IdMaskJackson.LongSerializer {
 *          public Serializer() {
 *              super(IdMasks.forLongIds(Config.builder(key).build()));
 *          }
 *      }
 *
 *      public static final class Deserializer extends IdMaskJackson.LongDeserializer {
 *          public Deserializer() {
 *              super(IdMasks.forLongIds(Config.builder(key).build()));
 *          }
 *      }
 * }
 * </pre>
 * <p>
 *
 * Annotate your model:
 *
 * <pre>
 * public class LongIdUser {
 *      &#64;JsonSerialize(using = MyIdMaskLongSerializers.Serializer.class)
 *      &#64;JsonDeserialize(using = MyIdMaskLongSerializers.Deserializer.class)
 *      private final long id;
 * ...
 * }
 * </pre>
 */
@SuppressWarnings("WeakerAccess")
public final class IdMaskJackson {
    private IdMaskJackson() {
    }

    /**
     * Used to serialize long to string
     */
    public static class LongSerializer extends Serializer<Long> {
        public LongSerializer(IdMask<Long> idMask) {
            super(idMask, Long.class);
        }
    }

    /**
     * Used to serialize {@link UUID} to string
     */
    public static class UuidSerializer extends Serializer<UUID> {
        public UuidSerializer(IdMask<UUID> idMask) {
            super(idMask, UUID.class);
        }
    }

    /**
     * Used to serialize {@link BigInteger} to string
     */
    public static class BigIntegerSerializer extends Serializer<BigInteger> {
        public BigIntegerSerializer(IdMask<BigInteger> idMask) {
            super(idMask, BigInteger.class);
        }
    }

    /**
     * Base serializer
     * @param <T> type to serialize
     */
    public abstract static class Serializer<T> extends StdSerializer<T> {
        private final IdMask<T> idMask;

        protected Serializer(IdMask<T> idMask, Class<T> clazz) {
            super(clazz);
            this.idMask = idMask;
        }

        @Override
        public void serialize(T aValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String maskedId = idMask.mask(aValue);
            jsonGenerator.writeString(maskedId);
        }
    }

    /**
     * Used to deserialize string to long
     */
    public static class LongDeserializer extends Deserializer<Long> {
        public LongDeserializer(IdMask<Long> idMask) {
            super(idMask, Long.class);
        }
    }

    /**
     * Used to deserialize string to {@link UUID}
     */
    public static class UuidDeserializer extends Deserializer<UUID> {
        public UuidDeserializer(IdMask<UUID> idMask) {
            super(idMask, UUID.class);
        }
    }

    /**
     * Used to deserialize string to {@link BigInteger}
     */
    public static class BigIntegerDeserializer extends Deserializer<BigInteger> {
        public BigIntegerDeserializer(IdMask<BigInteger> idMask) {
            super(idMask, BigInteger.class);
        }
    }

    /**
     * Base deserializer
     * @param <T> type to serialize
     */
    public abstract static class Deserializer<T> extends StdDeserializer<T> {
        private final IdMask<T> idMask;

        protected Deserializer(IdMask<T> idMask, Class<T> vc) {
            super(vc);
            this.idMask = idMask;
        }

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return idMask.unmask(jp.getValueAsString());
        }
    }
}
