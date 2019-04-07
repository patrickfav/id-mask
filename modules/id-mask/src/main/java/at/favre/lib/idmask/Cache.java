package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import net.markenwerk.utils.lrucache.LruCache;

/**
 * Cache responsible for caching masked ids
 */
public interface Cache {

    /**
     * Puts a new id and it's encoded form in the cache.
     *
     * @param originalId the id
     * @param encoded    the masked/encoded form of originalId
     */
    void cache(byte[] originalId, String encoded);

    /**
     * Retrieve the encoded form of given original id. This is used when encoding/masking.
     *
     * @param originalId to get cache content for
     * @return encoded version of originalId if found, null otherwise
     */
    String getEncoded(byte[] originalId);

    /**
     * Retrieve the originalId form of given encoded id. This is used when decoding/unmasking.
     *
     * @param encoded to get cache content for
     * @return original id of encoded id if found, null otherwise
     */
    byte[] getBytes(String encoded);

    /**
     * Clears internal cache.
     */
    void clear();

    /**
     * Simple In-Memory LRU cache.
     * Per default has {@link #CACHE_SIZE} size for mask &amp; unmask cache.
     * <p>
     * See: https://en.wikipedia.org/wiki/Cache_replacement_policies#Least_recently_used_(LRU)
     */
    @SuppressWarnings("WeakerAccess")
    final class SimpleLruMemCache implements Cache {
        private static final int CACHE_SIZE = 256;
        private final LruCache<CharSequence, String> lruCacheEncode;
        private final LruCache<CharSequence, byte[]> lruCacheDecode;

        public SimpleLruMemCache() {
            this(CACHE_SIZE);
        }

        public SimpleLruMemCache(int size) {
            lruCacheEncode = new LruCache<>(size);
            lruCacheDecode = new LruCache<>(size);
        }

        @Override
        public void cache(byte[] originalId, String encoded) {
            lruCacheEncode.put(Bytes.wrap(originalId).hashSha256().encodeBase64(), encoded);
            lruCacheDecode.put(encoded, originalId);
        }

        @Override
        public String getEncoded(byte[] originalId) {
            return lruCacheEncode.get(Bytes.wrap(originalId).hashSha256().encodeBase64());
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
