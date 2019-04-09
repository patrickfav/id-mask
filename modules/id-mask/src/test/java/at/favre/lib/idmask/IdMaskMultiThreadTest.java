package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IdMaskMultiThreadTest extends AMultiThreadTest {

    @Test
    public void testLongIdsNoCache() throws InterruptedException {
        final IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder(Bytes.random(16).array()).enableCache(false).build());
        testWithLongIds(idMask);
    }

    private void testWithLongIds(final IdMask<Long> idMask) throws InterruptedException {
        for (int i = 0; i < ROUNDS; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    Long id = Bytes.random(8).toLong();
                    String maskedId = idMask.mask(id);
                    assertNotNull(maskedId);
                    assertEquals(id, idMask.unmask(maskedId));
                }
            });
        }

        shutdown();
    }

    @Test
    public void testLongIdsWithCache() throws InterruptedException {
        final IdMask<Long> idMask = new IdMask.LongIdMask(Config.builder(Bytes.random(16).array()).enableCache(true).build());
        testWithLongIds(idMask);
    }

    @Test
    public void testUuidIds() throws InterruptedException {
        final IdMask<UUID> idMask = new IdMask.UuidMask(Config.builder(Bytes.random(16).array()).enableCache(false).build());

        for (int i = 0; i < ROUNDS; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    UUID uuid = UUID.randomUUID();
                    String maskedId = idMask.mask(uuid);
                    assertNotNull(maskedId);
                    assertEquals(uuid, idMask.unmask(maskedId));
                }
            });
        }

        shutdown();
    }
}
