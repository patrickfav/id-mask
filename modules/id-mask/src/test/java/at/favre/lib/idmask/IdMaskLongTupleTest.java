package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongTupleTest {
    private IdMask<LongTuple> idMask = new IdMask.LongIdTupleMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            LongTuple tuple = new LongTuple(r.nextLong(), r.nextLong());
            String encoded = idMask.mask(tuple);
            LongTuple refId = idMask.unmask(encoded);
            assertEquals(tuple, refId);

            System.out.println(encoded);
        }
    }

}
