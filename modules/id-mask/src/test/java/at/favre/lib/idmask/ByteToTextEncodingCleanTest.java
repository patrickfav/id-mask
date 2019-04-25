package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

public class ByteToTextEncodingCleanTest {
    private final ByteToTextEncoding encoding = new ByteToTextEncoding.CleanBase32Encoding();

    @Test
    public void testRandomForWordOccurrence() {
        for (int i = 0; i < 128; i++) {
            System.out.println(encoding.encode(Bytes.random(64).array()));
        }
    }
}
