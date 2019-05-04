package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class ByteToTextEncodingIdFormatterTest {

    public ByteToTextEncodingIdFormatterTest() {
    }

    @Test
    public void testFormattedEncoding() {
        checkEncoding(ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648(), 5, ".."));
        checkEncoding(ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648(), 7));
        checkEncoding(ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648()));
        checkEncoding(ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base16()));
    }

    private void checkEncoding(ByteToTextEncoding encoding) {
        byte[] random = Bytes.random(17).array();
        String encoded = encoding.encode(random);
        assertArrayEquals(random, encoding.decode(encoded));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalIntervalLength() {
        ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSepLength_tooShort() {
        ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648(), 2, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSepLength_tooLong() {
        ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base32Rfc4648(), 2, "1234567");
    }

    @Test
    public void testIllegalSeparator() {
        ByteToTextEncoding encoding = ByteToTextEncoding.IdFormatter.wrap(new ByteToTextEncoding.Base64Url());
        try {
            encoding.encode(Bytes.parseBase64("Cu7sUU8fe-Je").array());
            fail();
        } catch (IllegalArgumentException ignored) {

        }
    }
}
