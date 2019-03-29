package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class IdMaskEngineTest {

    @Test
    public void test() {
        byte[] id = Bytes.from(397849238741629487L).array();
        for (Mode mode : Mode.values()) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.Default(Bytes.from(192731092837120938L).array(), mode);
            for (int i = 0; i < 100; i++) {
                String maskedId = idMaskEngine.mask(id);
                assertNotNull(maskedId);
                assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                System.out.println(maskedId);
            }
        }
    }

}
