package at.favre.lib.idmask.ext;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMask;
import at.favre.lib.idmask.IdMasks;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

public final class IdMaskJackson {

    public static class LongSerializer extends Serializer<Long> {
        public LongSerializer() {
            this(IdMasks.forLongIds(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public LongSerializer(IdMask<Long> idMask) {
            super(idMask, Long.class);
        }
    }

    public static class UuidSerializer extends Serializer<UUID> {
        public UuidSerializer() {
            this(IdMasks.forUuids(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public UuidSerializer(IdMask<UUID> idMask) {
            super(idMask, UUID.class);
        }
    }

    public static class BigIntegerSerializer extends Serializer<BigInteger> {
        public BigIntegerSerializer() {
            this(IdMasks.forBigInteger(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public BigIntegerSerializer(IdMask<BigInteger> idMask) {
            super(idMask, BigInteger.class);
        }
    }

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

    public static class LongDeserializer extends Deserializer<Long> {
        public LongDeserializer() {
            this(IdMasks.forLongIds(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public LongDeserializer(IdMask<Long> idMask) {
            super(idMask, Long.class);
        }
    }

    public static class UuidDeserializer extends Deserializer<UUID> {
        public UuidDeserializer() {
            this(IdMasks.forUuids(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public UuidDeserializer(IdMask<UUID> idMask) {
            super(idMask, UUID.class);
        }
    }

    public static class BigIntegerDeserializer extends Deserializer<BigInteger> {
        public BigIntegerDeserializer() {
            this(IdMasks.forBigInteger(Config.builder(Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array()).build()));
        }

        public BigIntegerDeserializer(IdMask<BigInteger> idMask) {
            super(idMask, BigInteger.class);
        }
    }

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
