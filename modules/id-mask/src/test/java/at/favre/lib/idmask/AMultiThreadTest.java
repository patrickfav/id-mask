package at.favre.lib.idmask;

import org.junit.Before;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AMultiThreadTest {
    static final int ROUNDS = 2000;
    ExecutorService executor;

    @Before
    public void setup() {
        executor = Executors.newFixedThreadPool(32);
    }

    void shutdown() throws InterruptedException {
        Thread.sleep(500);

        executor.shutdown();
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }
}
