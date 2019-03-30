package at.favre.lib.idmask;

import org.hashids.Hashids;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HashIdPlayground {
    private Hashids hashids = new Hashids("ß8rgßpwhrüp9üskdgj");

    @Test
    public void testRepeatingLongs() {
        String encoded = hashids.encode(1, 1, 1, 1);

        String encoded1 = hashids.encode(1);
        String encoded2 = hashids.encode(1);

        System.out.println(encoded);
        System.out.println(encoded1);

        assertNotEquals(encoded, encoded1);
        assertEquals(encoded1, encoded2);
    }
}
