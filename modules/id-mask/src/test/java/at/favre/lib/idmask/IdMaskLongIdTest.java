package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongIdTest {
    private IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.withKey(Bytes.random(16).array())).cacheDecode(false).cacheEncode(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            long id = new Random().nextLong();
            String encoded = idMask.encode(id);
            long refId = idMask.decode(encoded);
            assertEquals(id, refId);

            System.out.println(encoded);
        }
    }

    @Test
    public void testEncodeDecodeInt() {
        for (int i = 0; i < 10; i++) {
            int id = new Random().nextInt();
            String encoded = idMask.encode((long) id);
            long refId = idMask.decode(encoded);
            assertEquals(id, refId);

            System.out.println(encoded);
        }
    }

    @Test
    public void testDecodeCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.withKey(Bytes.random(16).array())).cacheDecode(true).cacheEncode(false).build());
        long id = new Random().nextLong();
        String encoded = idMask.encode(id);

        for (int i = 0; i < 10000; i++) {
            long refId = idMask.decode(encoded);
            assertEquals(id, refId);
        }
    }

    @Test
    public void testEncodeCache() {
        IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().keyManager(KeyManager.Factory.withKey(Bytes.random(16).array())).cacheDecode(false).cacheEncode(true).build());
        long id = new Random().nextLong();
        String encoded = idMask.encode(id);

        for (int i = 0; i < 10000; i++) {
            String encodedRef = idMask.encode(id);
            assertEquals(encoded, encodedRef);
        }
    }

}
