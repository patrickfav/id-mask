package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class IdMaskEngineMultiThreadTest extends AMultiThreadTest {

    @Test
    public void test16Byte() throws InterruptedException {
        final IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(
                KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE);
        for (int i = 0; i < ROUNDS; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    byte[] id = Bytes.random(16).array();
                    CharSequence maskedId = idMaskEngine.mask(id);
                    assertNotNull(maskedId);
                    assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                }
            });
        }

        shutdown();
    }

    @Test
    public void test8Byte() throws InterruptedException {
        final IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(
                KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        for (int i = 0; i < ROUNDS; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    byte[] id = Bytes.random(8).array();
                    CharSequence maskedId = idMaskEngine.mask(id);
                    assertNotNull(maskedId);
                    assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                }
            });
        }

        shutdown();
    }
}
