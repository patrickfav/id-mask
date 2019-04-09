package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

@RunWith(Parameterized.class)
public class ByteToTextEncodingTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new ByteToTextEncoding.Base64Url()}, {new ByteToTextEncoding.Base32Rfc4648()},
                {new ByteToTextEncoding.SafeBase32Encoding()}, {new ByteToTextEncoding.Base16()}
        });
    }

    private final ByteToTextEncoding encoding;

    public ByteToTextEncodingTest(ByteToTextEncoding encoding) {
        this.encoding = encoding;
    }

    @Test
    public void testEncoding() {
        for (int i = 1; i < 64; i++) {
            byte[] random = Bytes.random(i).array();
            String encoded = encoding.encode(random);
            assertArrayEquals(random, encoding.decode(encoded));

            System.out.println(encoded);
        }
    }

}
