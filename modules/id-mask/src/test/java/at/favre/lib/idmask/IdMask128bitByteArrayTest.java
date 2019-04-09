package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class IdMask128bitByteArrayTest {
    private IdMask<byte[]> idMask = new IdMask.ByteArray128bitMask(
            Config.builder(Bytes.random(16).array())
                    .enableCache(false)
                    .build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            byte[] id = Bytes.random(16).array();
            String encoded = idMask.mask(id);
            byte[] refId = idMask.unmask(encoded);
            assertArrayEquals(id, refId);

            System.out.println(encoded);
        }
    }

}
