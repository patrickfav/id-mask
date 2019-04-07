package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongIdTest {
    private IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            long id = new Random().nextLong();
            String encoded = idMask.mask(id);
            long refId = idMask.unmask(encoded);
            assertEquals(id, refId);

            System.out.println(encoded);
        }
    }

    @Test
    public void testEncodeDecodePrimitiveInt() {
        for (int i = 0; i < 10; i++) {
            int id = new Random().nextInt();
            String encoded = idMask.mask((long) id);
            long refId = idMask.unmask(encoded);
            assertEquals(id, refId);

            System.out.println(encoded);
        }
    }

    @Test
    public void testEncodeDecodeBoxedInt() {
        for (int i = 0; i < 10; i++) {
            //noinspection WrapperTypeMayBePrimitive
            Integer id = new Random().nextInt();
            String encoded = idMask.mask(id.longValue());
            long refId = idMask.unmask(encoded);
            assertEquals(id.longValue(), refId);
        }
    }

    @Test
    public void testDecodeCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(true).build());
        long id = new Random().nextLong();
        String encoded = idMask.mask(id);

        for (int i = 0; i < 10000; i++) {
            long refId = idMask.unmask(encoded);
            assertEquals(id, refId);
        }
    }

    @Test
    public void testEncodeCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());
        long id = new Random().nextLong();
        String encoded = idMask.mask(id);

        for (int i = 0; i < 10000; i++) {
            CharSequence encodedRef = idMask.mask(id);
            assertEquals(encoded, encodedRef);
        }
    }

}
