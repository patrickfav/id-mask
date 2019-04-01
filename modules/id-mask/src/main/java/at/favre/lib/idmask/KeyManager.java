package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import java.util.*;

public interface KeyManager {

    IdKey getById(int id);

    IdKey getActiveKey();

    final class Factory {
        private Factory() {
        }

        public static KeyManager withKey(IdKey activeKey) {
            return new KeyManager.Default(activeKey);
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
            return keys.get(activeKeyId);
        }
    }

    final class IdKey {
        private final int keyId;
        private final byte[] keyBytes;

        public IdKey(int keyId, byte[] keyBytes) {
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
