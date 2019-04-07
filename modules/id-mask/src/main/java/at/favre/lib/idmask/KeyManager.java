package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import javax.crypto.SecretKey;
import java.util.*;

import static at.favre.lib.bytes.BytesValidators.*;
import static at.favre.lib.idmask.IdMaskEngine.MAX_KEY_ID;

/**
 * Manages secret keys of {@link IdMask}. This either encapsulates a single
 * active key and optionally multiple legacy keys used to decrypt content
 * encrypted with it. Keys are identified with key ids. Depending on the
 * implementation, given key might not be used directly, but a derived version.
 */
public interface KeyManager {

    /**
     * Gets an id-key for given key id.
     *
     * @param id to find the key by
     * @return key or null if not found
     */
    IdSecretKey getById(int id);

    /**
     * Get current active key. This is the key used to encrypt new ids.
     *
     * @return active secret key
     */
    IdSecretKey getActiveKey();

    /**
     * Get the key id of currents active secret key.
     *
     * @return key id
     */
    int getActiveKeyId();

    /**
     * Size of currently managed keys including active key.
     *
     * @return size of managed keys
     */
    int size();

    /**
     * Clears each individual key (calls {@link IdSecretKey#clear()} on every key) and
     * removes all managed keys.
     */
    void clear();

    /**
     * Factory and main API for creating new key managers.
     */
    @SuppressWarnings("WeakerAccess")
    final class Factory {
        /**
         * If no explicit id is used, this is the used default key id
         */
        private static final int DEFAULT_KEY_ID = 0;

        private Factory() {
        }

        /**
         * Creates a new key manager with a single id key, which will be used as active key.
         * <p>
         * A key must be between 8 and 64 bytes, must not only contains zeros and must
         * have reasonable entropy (check with 'Bytes.wrap(key).entropy()').
         *
         * @param activeKey to use
         * @return manager
         */
        public static KeyManager with(IdSecretKey activeKey) {
            return new KeyManager.Default(activeKey);
        }

        /**
         * Creates a new key manager with a single key and id, which will be used as active key.
         *
         * @param id        for given key which must be between 0 and 15 (within 4 bit)
         * @param secretKey used to encrypt which must be between 8 and 64 bytes, must not
         *                  only contains zeros and must have reasonable entropy
         *                  (check with 'Bytes.wrap(key).entropy()').
         * @return manager
         */
        public static KeyManager with(int id, byte[] secretKey) {
            return new KeyManager.Default(new IdSecretKey(id, secretKey));
        }

        /**
         * Creates a new key manager with a single key and default id, which will be used as active key.
         *
         * @param secretKey used to encrypt which must be between 8 and 64 bytes, must not
         *                  only contains zeros and must have reasonable entropy
         *                  (check with 'Bytes.wrap(key).entropy()').
         * @return manager
         */
        public static KeyManager with(byte[] secretKey) {
            return new KeyManager.Default(new IdSecretKey(DEFAULT_KEY_ID, secretKey));
        }

        /**
         * Creates a new key manager with a single key and default id, which will be used as active key.
         *
         * @param secretAesKey used to encrypt which should be an AES key and should be either 16 or 32
         *                     byte must not only contains zeros and must have reasonable entropy
         *                     (check with 'Bytes.wrap(key).entropy()').
         * @return manager
         */
        public static KeyManager with(SecretKey secretAesKey) {
            return new KeyManager.Default(new IdSecretKey(DEFAULT_KEY_ID, secretAesKey.getEncoded()));
        }

        /**
         * Creates a new key manager with a single active key and multiple,
         * keys which are supported for decryption.
         *
         * @param activeKey  used to encrypt new data
         * @param legacyKeys to support decryption of older ids
         * @return manager
         */
        public static KeyManager withKeyAndLegacyKeys(IdSecretKey activeKey, IdSecretKey... legacyKeys) {
            return new KeyManager.Default(activeKey, Objects.requireNonNull(legacyKeys, "legacyKeys"));
        }

        /**
         * This is only for internal use. Uses a 64 bit integer as 8 byte long key.
         *
         * @param key used to encrypt.
         * @return manager
         */
        static KeyManager with(long key) {
            return new KeyManager.Default(new IdSecretKey(DEFAULT_KEY_ID, Bytes.from(key).array()));
        }

        /**
         * This is only for internal use. Creates a random 16 byte long key
         *
         * @return manager
         */
        static KeyManager withRandom() {
            return new KeyManager.Default(new IdSecretKey(DEFAULT_KEY_ID, Bytes.random(16).array()));
        }
    }

    /**
     * Default implementation
     */
    final class Default implements KeyManager {
        private final Map<Integer, IdSecretKey> keys = new HashMap<>(3);
        private final int activeKeyId;

        Default(IdSecretKey activeKey, IdSecretKey... moreKeys) {
            activeKeyId = Objects.requireNonNull(activeKey, "activeKey").keyId;
            List<IdSecretKey> list = new ArrayList<>(1 + (moreKeys != null ? moreKeys.length : 0));
            list.add(activeKey);
            if (moreKeys != null) {
                list.addAll(Arrays.asList(moreKeys));
            }

            for (IdSecretKey idSecretKey : list) {
                if (keys.containsKey(idSecretKey.keyId)) {
                    throw new IllegalArgumentException("key with id " + idSecretKey.keyId + " already added");
                }
                keys.put(idSecretKey.keyId, Objects.requireNonNull(idSecretKey, "idSecretKey " + idSecretKey.keyId));
            }
        }

        @Override
        public IdSecretKey getById(int id) {
            return keys.get(id);
        }

        @Override
        public IdSecretKey getActiveKey() {
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
            for (IdSecretKey value : keys.values()) {
                value.clear();
            }
            keys.clear();
        }
    }

    /**
     * Used as cached key derivation proxy.
     */
    final class CachedKdfConverter implements KeyManager {

        static KeyManager wrap(KeyManager keyManager, KdfConverter converter) {
            return new CachedKdfConverter(keyManager, converter);
        }

        private final Map<Integer, byte[]> cache;
        private final KeyManager keyManager;
        private final KdfConverter converter;

        CachedKdfConverter(KeyManager keyManager, KdfConverter converter) {
            this.keyManager = Objects.requireNonNull(keyManager, "keyManager");
            this.converter = Objects.requireNonNull(converter, "converter");
            this.cache = new HashMap<>(keyManager.size());
        }

        @Override
        public IdSecretKey getById(int id) {
            if (!cache.containsKey(id)) {
                final IdSecretKey k;
                if ((k = keyManager.getById(id)) != null) {
                    cache.put(id, converter.convert(k));
                } else {
                    return null;
                }
            }
            return new IdSecretKey(id, cache.get(id));
        }

        @Override
        public IdSecretKey getActiveKey() {
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

        public interface KdfConverter {
            byte[] convert(IdSecretKey idSecretKey);
        }
    }

    /**
     * Model representing a secret key for encrypting ids and a key id for
     * later matching the id.
     */
    @SuppressWarnings("WeakerAccess")
    final class IdSecretKey {
        private final int keyId;
        private final byte[] keyBytes;

        /**
         * Create new instance.
         *
         * @param keyId    for given key which must be between 0 and 15 (within 4 bit)
         * @param keyBytes used to encrypt which must be between 8 and 64 bytes, must not
         *                 only contains zeros and must have reasonable entropy
         *                 (check with 'Bytes.wrap(key).entropy()').
         */
        public IdSecretKey(int keyId, byte[] keyBytes) {
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

        /**
         * Id of the key
         *
         * @return id (0-15)
         */
        public int getKeyId() {
            return keyId;
        }

        /**
         * Raw bytes of the key
         *
         * @return bytes
         */
        public byte[] getKeyBytes() {
            return keyBytes;
        }

        /**
         * Clears key bytes (i.e. overwrites with random data)
         */
        public void clear() {
            Bytes.wrap(keyBytes).mutable().secureWipe();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IdSecretKey idSecretKey = (IdSecretKey) o;
            return keyId == idSecretKey.keyId &&
                    Arrays.equals(keyBytes, idSecretKey.keyBytes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(keyId);
            result = 31 * result + Arrays.hashCode(keyBytes);
            return result;
        }
    }
}
