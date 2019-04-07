package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import javax.crypto.SecretKey;
import java.util.*;

import static at.favre.lib.bytes.BytesValidators.*;
import static at.favre.lib.idmask.IdMaskEngine.MAX_KEY_ID;

public interface KeyManager {

    IdKey getById(int id);

    IdKey getActiveKey();

    int getActiveKeyId();

    int size();

    void clear();

    @SuppressWarnings("WeakerAccess")
    final class Factory {
        private static final int DEFAULT_KEY_ID = 0;

        private Factory() {
        }

        public static KeyManager with(IdKey activeKey) {
            return new KeyManager.Default(activeKey);
        }

        public static KeyManager with(int id, byte[] key) {
            return new KeyManager.Default(new IdKey(id, key));
        }

        public static KeyManager with(byte[] secretKey) {
            return new KeyManager.Default(new IdKey(DEFAULT_KEY_ID, secretKey));
        }

        public static KeyManager with(SecretKey secretAesKey) {
            return new KeyManager.Default(new IdKey(DEFAULT_KEY_ID, secretAesKey.getEncoded()));
        }

        static KeyManager with(long key) {
            return new KeyManager.Default(new IdKey(DEFAULT_KEY_ID, Bytes.from(key).array()));
        }

        static KeyManager withRandom() {
            return new KeyManager.Default(new IdKey(DEFAULT_KEY_ID, Bytes.random(16).array()));
        }

        public static KeyManager withKeyAndLegacyKeys(IdKey activeKey, IdKey... legacyKeys) {
            return new KeyManager.Default(activeKey, Objects.requireNonNull(legacyKeys, "legacyKeys"));
        }
    }

    final class Default implements KeyManager {
        private final Map<Integer, IdKey> keys = new HashMap<>(3);
        private final int activeKeyId;

        Default(IdKey activeKey, IdKey... moreKeys) {
            activeKeyId = Objects.requireNonNull(activeKey, "activeKey").keyId;
            List<IdKey> list = new ArrayList<>(1 + (moreKeys != null ? moreKeys.length : 0));
            list.add(activeKey);
            if (moreKeys != null) {
                list.addAll(Arrays.asList(moreKeys));
            }

            for (IdKey idKey : list) {
                if (keys.containsKey(idKey.keyId)) {
                    throw new IllegalArgumentException("key with id " + idKey.keyId + " already added");
                }
                keys.put(idKey.keyId, Objects.requireNonNull(idKey, "idKey " + idKey.keyId));
            }
        }

        @Override
        public IdKey getById(int id) {
            return keys.get(id);
        }

        @Override
        public IdKey getActiveKey() {
            return keys.get(getActiveKeyId());
        }

        @Override
        public int getActiveKeyId() {
            return activeKeyId;
        }

        @Override
        public int size() {
            return keys.size();
        }

        @Override
        public void clear() {
            for (IdKey value : keys.values()) {
                value.clear();
            }
            keys.clear();
        }
    }

    final class CachedValueConverter implements KeyManager {

        static KeyManager wrap(KeyManager keyManager, ValueConverter converter) {
            return new CachedValueConverter(keyManager, converter);
        }

        private final Map<Integer, byte[]> cache;
        private final KeyManager keyManager;
        private final ValueConverter converter;

        private CachedValueConverter(KeyManager keyManager, ValueConverter converter) {
            this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
            this.converter = Objects.requireNonNull(converter, "converter");
            this.cache = new HashMap<>(keyManager.size());
        }

        @Override
        public IdKey getById(int id) {
            if (!cache.containsKey(id)) {
                final IdKey k;
                if ((k = keyManager.getById(id)) != null) {
                    cache.put(id, converter.convert(k));
                } else {
                    return null;
                }
            }
            return new IdKey(id, cache.get(id));
        }

        @Override
        public IdKey getActiveKey() {
            return getById(getActiveKeyId());
        }

        @Override
        public int getActiveKeyId() {
            return keyManager.getActiveKeyId();
        }

        @Override
        public int size() {
            return keyManager.size();
        }

        @Override
        public void clear() {
            keyManager.clear();
        }

        public interface ValueConverter {
            byte[] convert(IdKey idKey);
        }
    }

    @SuppressWarnings("WeakerAccess")
    final class IdKey {
        private final int keyId;
        private final byte[] keyBytes;

        public IdKey(int keyId, byte[] keyBytes) {
            Bytes bytes = Bytes.wrap(keyBytes);
            if (!bytes.validate(
                    and(atLeast(8), atMost(64), notOnlyOf((byte) 0))
            ) || bytes.entropy() < 2.5) {
                throw new IllegalArgumentException("key must be at least 8 byte, at most 64 byte and must not only contain zeros, also must have high entropy");
            }

            if (keyId < 0 || keyId > MAX_KEY_ID) {
                throw new IllegalArgumentException("key id must be between 0 and 16");
            }

            this.keyId = keyId;
            this.keyBytes = keyBytes;
        }

        public int getKeyId() {
            return keyId;
        }

        public byte[] getKeyBytes() {
            return keyBytes;
        }

        public void clear() {
            Bytes.wrap(keyBytes).mutable().secureWipe();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IdKey idKey = (IdKey) o;
            return keyId == idKey.keyId &&
                    Arrays.equals(keyBytes, idKey.keyBytes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keyId);
            result = 31 * result + Arrays.hashCode(keyBytes);
            return result;
        }
    }
}
