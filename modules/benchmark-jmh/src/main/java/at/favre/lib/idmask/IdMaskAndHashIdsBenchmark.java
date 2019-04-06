package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.hashids.Hashids;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
        # Run complete. Total time: 00:04:05

        REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
        why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
        experiments, perform baseline and negative tests that provide experimental control, make sure
        the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
        Do not assume the numbers tell you what you want them to tell.

        Benchmark                                               Mode  Cnt      Score       Error  Units
        IdMaskAndHashIdsBenchmark.benchmarkHashIdEncode         avgt    3      4,438 ±     5,518  ns/op
        IdMaskAndHashIdsBenchmark.benchmarkHashIdEncodeDecode   avgt    3      5,643 ±     2,432  ns/op
        IdMaskAndHashIdsBenchmark.benchmarkIdMask16Byte         avgt    3  10235,168 ± 14503,867  ns/op
        IdMaskAndHashIdsBenchmark.benchmarkIdMask8Byte          avgt    3   1292,331 ±   371,898  ns/op
        IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask16Byte  avgt    3  16754,766 ±  1115,565  ns/op
        IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask8Byte   avgt    3   1891,529 ±   311,644  ns/op

        Process finished with exit code 0
*/

@SuppressWarnings("CheckStyle")
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class IdMaskAndHashIdsBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        private long id;
        private IdMaskEngine idMaskEngine;
        private IdMaskEngine idMaskEngine16Byte;
        private Hashids hashids;

        @Setup
        public void setup() {
            //noinspection StatementWithEmptyBody
            while ((id = new Random().nextLong()) > 9005199254740992L) ;

            idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(Bytes.random(16).array()));
            idMaskEngine16Byte = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(Bytes.random(16).array()));
            hashids = new Hashids(Bytes.random(16).encodeBase64());
        }
    }

    @Benchmark
    public void benchmarkIdMask8Byte(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.idMaskEngine.mask(Bytes.from(state.id).array()));
        state.id++;
    }

    @Benchmark
    public void benchmarkIdMask16Byte(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.idMaskEngine16Byte.mask(Bytes.from(0L, state.id).array()));
        state.id++;
    }

    @Benchmark
    public void benchmarkHashIdEncode(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.hashids.encode(state.id));
        state.id++;
    }

    @Benchmark
    public void benchmarkMaskAndUnmask8Byte(BenchmarkState state, Blackhole blackhole) {
        String encoded = state.idMaskEngine.mask(Bytes.from(state.id).array());
        blackhole.consume(state.idMaskEngine.unmask(encoded));
        state.id++;
    }

    @Benchmark
    public void benchmarkMaskAndUnmask16Byte(BenchmarkState state, Blackhole blackhole) {
        String encoded = state.idMaskEngine16Byte.mask(Bytes.from(0L, state.id).array());
        blackhole.consume(state.idMaskEngine16Byte.unmask(encoded));
        state.id++;
    }

    @Benchmark
    public void benchmarkHashIdEncodeDecode(BenchmarkState state, Blackhole blackhole) {
        String encoded = state.hashids.encode(state.id);
        blackhole.consume(state.hashids.decode(encoded));
        state.id++;
    }
}
