package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BaseEncodingTest {

    @Test
    public void encode() {
        BaseEncoding baseEncoding = new BaseEncoding(new BaseEncoding.Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()), '=');
        for (int i = 0; i < 128; i++) {
            byte[] msg = Bytes.random(i).array();
            CharSequence e = baseEncoding.encode(msg);
            byte[] orig = baseEncoding.decode(e);
            assertArrayEquals(msg, orig);
        }
    }
}
