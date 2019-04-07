package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IdMaskFactoryTest {

    @Test
    public void createForLongIds() {
        long ref = 6L;
        IdMask<Long> idMask = IdMaskFactory.createForLongIds(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).build());
        String encoded = idMask.mask(ref);
        long decoded = idMask.unmask(encoded);

        assertEquals(ref, decoded);
    }

    @Test
    public void createForLongTuples() {
        LongTuple tuple = new LongTuple(41L, 19283183891L);
        IdMask<LongTuple> idMask = IdMaskFactory.createForLongTuples(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).build());
        String encoded = idMask.mask(tuple);
        LongTuple decoded = idMask.unmask(encoded);

        assertEquals(tuple, decoded);
    }

    @Test
    public void createForUuids() {
        UUID uuid = UUID.randomUUID();
        IdMask<UUID> idMask = IdMaskFactory.createForUuids(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).build());
        String encoded = idMask.mask(uuid);
        UUID decoded = idMask.unmask(encoded);

        assertEquals(uuid, decoded);
    }

    @Test
    public void createFo128bitNumbers() {
        byte[] id = Bytes.random(16).array();
        IdMask<byte[]> idMask = IdMaskFactory.createFor128bitNumbers(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).build());
        String encoded = idMask.mask(id);
        byte[] decoded = idMask.unmask(encoded);

        assertArrayEquals(id, decoded);
    }
}
