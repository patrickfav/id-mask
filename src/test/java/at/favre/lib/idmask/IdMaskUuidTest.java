package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class IdMaskUuidTest {
    private IdMask<UUID> idMask = new IdMask.UuidMask(Config.builder().key(Bytes.random(16).array()).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            UUID uuid = UUID.randomUUID();
            String encoded = idMask.encode(uuid);
            UUID refId = idMask.decode(encoded);
            assertEquals(uuid, refId);

            System.out.println(encoded);
        }
    }

}
