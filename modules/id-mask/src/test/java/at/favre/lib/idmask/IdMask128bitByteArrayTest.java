package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

public class IdMask128bitByteArrayTest extends ABaseIdMaskTest {
    private IdMask<byte[]> idMask = new IdMask.ByteArray128bitMask(Config.builder(Bytes.random(16).array()).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, Bytes.random(16).array());
        }
    }
}
