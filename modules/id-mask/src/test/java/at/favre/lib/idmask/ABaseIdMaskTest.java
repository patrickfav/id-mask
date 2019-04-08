package at.favre.lib.idmask;

import static org.junit.Assert.assertEquals;

public abstract class ABaseIdMaskTest {

    <T> void maskAndUnmask(IdMask<T> idMask, T id) {
        String encoded = idMask.mask(id);
        T refId = idMask.unmask(encoded);
        assertEquals(id, refId);

        System.out.println(encoded);
    }
}
