package at.favre.lib.idmaskbench;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMask;
import at.favre.lib.idmask.IdMasks;
import org.hashids.Hashids;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
    # Run complete. Total time: 00:04:02

    REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
    why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
    experiments, perform baseline and negative tests that provide experimental control, make sure
    the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
    Do not assume the numbers tell you what you want them to tell.

    Benchmark                                               Mode  Cnt      Score     Error  Units
    IdMaskAndHashIdsBenchmark.benchmarkHashIdEncode         avgt    3      2,306 ±   0,030  ns/op
    IdMaskAndHashIdsBenchmark.benchmarkHashIdEncodeDecode   avgt    3      3,174 ±   0,023  ns/op
    IdMaskAndHashIdsBenchmark.benchmarkIdMask16Byte         avgt    3   7411,538 ±  67,600  ns/op
    IdMaskAndHashIdsBenchmark.benchmarkIdMask8Byte          avgt    3   2012,647 ±   9,976  ns/op
    IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask16Byte  avgt    3  15035,381 ± 280,808  ns/op
    IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask8Byte   avgt    3   4161,346 ±  28,803  ns/op

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
        private IdMask<Long> idMaskEngine;
        private IdMask<byte[]> idMaskEngine16Byte;
        private Hashids hashids;

        @Setup
        public void setup() {
            // check that ids are beneath HashIds max supported number
            do {
                id = new Random().nextLong();
            } while (id > Hashids.MAX_NUMBER - (long) Integer.MAX_VALUE);

            idMaskEngine = IdMasks.forLongIds(
                    Config.builder(Bytes.random(16).array())
                            .enableCache(false)
                            .build());
            idMaskEngine16Byte = IdMasks.for128bitNumbers(
                    Config.builder(Bytes.random(16).array())
                            .enableCache(false)
                            .build());
            hashids = new Hashids(Bytes.random(16).encodeBase64());
        }
    }

    @Benchmark
    public void benchmarkIdMask8Byte(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(state.idMaskEngine.mask(state.id));
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
        String encoded = state.idMaskEngine.mask(state.id);
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
