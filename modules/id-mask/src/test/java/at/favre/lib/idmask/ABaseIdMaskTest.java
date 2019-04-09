package at.favre.lib.idmask;

import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class ABaseIdMaskTest {
    List<ByteToTextEncoding> encodings;

    @Before
    public void setup() {
        encodings = new ArrayList<>();
        encodings.addAll(Arrays.asList(
                new ByteToTextEncoding.Base16(),
                new ByteToTextEncoding.Base32Rfc4648(),
                new ByteToTextEncoding.CleanBase32Encoding(),
                new ByteToTextEncoding.Base64Url()));
    }

    <T> void maskAndUnmask(IdMask<T> idMask, T id) {
        String encoded = idMask.mask(id);
        T refId = idMask.unmask(encoded);
        assertEquals(id, refId);

        System.out.println(encoded);
    }
}
