package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class IdMaskEngineMultiThreadTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(24);

    @Test
    public void test16Byte() throws InterruptedException {
        final IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(192731092837120938L).array());
        for (int i = 0; i < 1600; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    byte[] id = Bytes.random(16).array();
                    String maskedId = idMaskEngine.mask(id);
                    assertNotNull(maskedId);
                    assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                }
            });
        }

        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

    }

    @Test
    public void test8Byte() throws InterruptedException {
        final IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(Bytes.from(192731092837120938L).array());
        for (int i = 0; i < 1600; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    byte[] id = Bytes.random(8).array();
                    String maskedId = idMaskEngine.mask(id);
                    assertNotNull(maskedId);
                    assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                }
            });
        }

        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

    }
}
