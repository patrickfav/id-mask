package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class IdMaskLongTupleTest {
    private IdMask<LongTuple> idMask = new IdMask.LongIdTupleMask(Config.builder().key(Bytes.random(16).array()).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            Random r = new Random();
            LongTuple tuple = new LongTuple(r.nextLong(), r.nextLong());
            String encoded = idMask.encode(tuple);
            LongTuple refId = idMask.decode(encoded);
            assertEquals(tuple, refId);

            System.out.println(encoded);
        }
    }

}
