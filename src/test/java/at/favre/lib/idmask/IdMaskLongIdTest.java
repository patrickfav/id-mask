package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongIdTest {
    private IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder().key(Bytes.random(16).array()).build());

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

}
