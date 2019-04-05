package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import java.util.UUID;

public interface IdMask<T> {

    String encode(T id);

    T decode(String encoded);

    abstract class BaseIdMask {
        private final IdMaskEngine engine;
        private final Config config;

        BaseIdMask(IdMaskEngine engine, Config config) {
            this.engine = engine;
            this.config = config;
        }

        String _encode(byte[] id) {
            String encoded;
            if (config.cacheEncode()) {
                if ((encoded = config.cacheImpl().getEncoded(id)) != null) {
                    return encoded;
                }
            }

            encoded = engine.mask(id);

            if (config.cacheEncode()) {
                config.cacheImpl().cache(id, encoded);
            }

            return encoded;
        }

        byte[] _decode(String encoded) {
            byte[] raw;
            if (config.cacheDecode()) {
                if ((raw = config.cacheImpl().getBytes(encoded)) != null) {
                    return Bytes.wrap(raw).copy().array();
                }
            }

            raw = engine.unmask(encoded);

            if (config.cacheEncode()) {
                config.cacheImpl().cache(raw, encoded);
            }

            return raw;
        }
    }

    final class LongIdMask extends BaseIdMask implements IdMask<Long> {

        LongIdMask(Config config) {
            super(new IdMaskEngine.EightByteEncryptionEngine(config.keyManager(), config.securityProvider(),
                    config.secureRandom(), config.encoding(), config.randomizedIds()), config);
        }

        @Override
        public String encode(Long id) {
            return _encode(Bytes.from(id).array());
        }

        @Override
        public Long decode(String encoded) {
            byte[] out = _decode(encoded);
            return Bytes.wrap(out).toLong();
        }
    }

    final class LongIdTupleMask extends BaseIdMask implements IdMask<LongTuple> {

        LongIdTupleMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String encode(LongTuple id) {
            return _encode(Bytes.from(id.getNum1(), id.getNum2()).array());
        }

        @Override
        public LongTuple decode(String encoded) {
            Bytes out = Bytes.wrap(_decode(encoded));
            return new LongTuple(out.longAt(0), out.longAt(8));
        }
    }

    final class UuidMask extends BaseIdMask implements IdMask<UUID> {

        UuidMask(Config config) {
            super(new IdMaskEngine.SixteenByteEngine(config.keyManager(), config.highSecurityMode(), config.encoding(),
                    config.secureRandom(), config.securityProvider(), config.randomizedIds()), config);
        }

        @Override
        public String encode(UUID id) {
            return _encode(Bytes.from(id).array());
        }

        @Override
        public UUID decode(String encoded) {
            byte[] out = _decode(encoded);
            return Bytes.wrap(out).toUUID();
        }
    }

}
