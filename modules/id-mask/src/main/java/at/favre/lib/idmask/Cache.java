package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import net.markenwerk.utils.lrucache.LruCache;

public interface Cache {

    void cache(byte[] raw, String encoded);

    String getEncoded(byte[] raw);

    byte[] getBytes(String encoded);

    void clear();

    @SuppressWarnings("WeakerAccess")
    final class SimpleLruCache implements Cache {
        private final LruCache<String, String> lruCacheEncode;
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
            lruCacheEncode.put(Bytes.wrap(raw).hashSha256().encodeBase64(), encoded);
            lruCacheDecode.put(encoded, raw);
        }

        @Override
        public String getEncoded(byte[] raw) {
            return lruCacheEncode.get(Bytes.wrap(raw).hashSha256().encodeBase64());
        }

        @Override
        public byte[] getBytes(String encoded) {
            return lruCacheDecode.get(encoded);
        }

        @Override
        public void clear() {
            lruCacheDecode.clear();
            lruCacheEncode.clear();
        }
    }
}
