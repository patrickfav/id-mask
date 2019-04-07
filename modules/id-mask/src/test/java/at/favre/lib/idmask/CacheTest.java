package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class CacheTest {
    private Cache cache = new Cache.SimpleLruMemCache(16);

    @Test
    public void testCacheEncode() {
        byte[] bytes = Bytes.random(4).array();
        String encoded = Bytes.wrap(bytes).encodeBase64();

        assertNull(cache.getEncoded(bytes));
        assertNull(cache.getBytes(encoded));

        cache.cache(bytes, encoded);

        assertEquals(encoded, cache.getEncoded(bytes));
        assertArrayEquals(bytes, cache.getBytes(encoded));
    }

    @Test
    public void testCacheEncodeRandom() {
        for (int i = 0; i < 256; i++) {
            byte[] bytes = Bytes.random(1).array();
            String encoded = Bytes.wrap(bytes).encodeBase64();

            cache.cache(bytes, encoded);

            assertEquals(encoded, cache.getEncoded(bytes));
            assertArrayEquals(bytes, cache.getBytes(encoded));
        }
    }

    @Test
    public void testClear() {
        byte[] bytes = Bytes.random(4).array();
        String encoded = Bytes.wrap(bytes).encodeBase64();

        assertNull(cache.getEncoded(bytes));
        assertNull(cache.getBytes(encoded));

        cache.cache(bytes, encoded);

        assertEquals(encoded, cache.getEncoded(bytes));
        assertArrayEquals(bytes, cache.getBytes(encoded));

        cache.clear();

        assertNull(cache.getEncoded(bytes));
        assertNull(cache.getBytes(encoded));
    }

}
