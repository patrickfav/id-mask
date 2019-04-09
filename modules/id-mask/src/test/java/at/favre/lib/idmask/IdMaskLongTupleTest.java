package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

public class IdMaskLongTupleTest extends ABaseIdMaskTest {
    private IdMask<LongTuple> idMask = new IdMask.LongIdTupleMask(Config.builder(Bytes.random(16).array()).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, new LongTuple(r.nextLong(), r.nextLong()));
        }
    }

}
