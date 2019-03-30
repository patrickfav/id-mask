package at.favre.lib.idmask;

import net.markenwerk.utils.lrucache.LruCache;

public interface Cache {

    void cache(byte[] raw, String encoded);

    String getEncoded(byte[] raw);

    byte[] getRaw(String encoded);

    @SuppressWarnings("WeakerAccess")
    final class SimpleLruCache implements Cache {
        private final LruCache<byte[], String> lruCacheEncode;
        private final LruCache<String, byte[]> lruCacheDecode;

        public SimpleLruCache() {
            this(512);
        }

        public SimpleLruCache(int size) {
            lruCacheEncode = new LruCache<>(size);
            lruCacheDecode = new LruCache<>(size);
        }

        @Override
        public void cache(byte[] raw, String encoded) {
            lruCacheEncode.put(raw, encoded);
            lruCacheDecode.put(encoded, raw);
        }

        @Override
        public String getEncoded(byte[] raw) {
            return lruCacheEncode.get(raw);
        }

        @Override
        public byte[] getRaw(String encoded) {
            return lruCacheDecode.get(encoded);
        }
    }
}
