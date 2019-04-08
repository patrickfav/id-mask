package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongIdTest extends ABaseIdMaskTest {
    private IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, new Random().nextLong());
        }
    }

    @Test
    public void testEncodeDecodePrimitiveInt() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, (long) new Random().nextInt());
        }
    }

    @Test
    public void testEncodeDecodeBoxedInt() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, Integer.valueOf(new Random().nextInt()).longValue());
        }
    }

    @Test
    public void testWithCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(true).build());
        long id = new Random().nextLong();
        String encoded = idMask.mask(id);

        for (int i = 0; i < 10000; i++) {
            long refId = idMask.unmask(encoded);
            assertEquals(id, refId);
        }
    }

    @Test
    public void testWithoutCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());
        long id = new Random().nextLong();
        String encoded = idMask.mask(id);

        for (int i = 0; i < 10000; i++) {
            CharSequence encodedRef = idMask.mask(id);
            assertEquals(encoded, encodedRef);
        }
    }

    @Test
    public void testEncodeDecodeNegativeLong() {
        maskAndUnmask(idMask, -87586759877098L);
    }

    @Test
    public void testEncodeDecodeMinAndMaxLongValue() {
        maskAndUnmask(idMask, Long.MAX_VALUE);
        maskAndUnmask(idMask, Long.MIN_VALUE);
    }
}
