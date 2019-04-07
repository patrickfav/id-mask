package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class IdMaskUuidTest {
    private IdMask<UUID> idMask = new IdMask.UuidMask(
            Config.builder()
                    .keyManager(KeyManager.Factory.with(Bytes.random(16).array()))
                    .enableCache(false)
                    .build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            UUID uuid = UUID.randomUUID();
            String encoded = idMask.mask(uuid);
            UUID refId = idMask.unmask(encoded);
            assertEquals(uuid, refId);

            System.out.println(encoded);
        }
    }

}
