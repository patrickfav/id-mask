package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.hashids.Hashids;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("CheckStyle")
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 10)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class IdMaskBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        @Param({"SMALL_SIZE_WEAK_SECURITY",
                "LARGER_SIZE_AND_VERY_HIGH_SECURITY"})
        private Mode mode;
        private long id;
        private IdMaskEngine idMaskEngine;
        private Hashids hashids;

        @Setup
        public void setup() {
            id = new Random().nextLong();
            idMaskEngine = new IdMaskEngine.Default(Bytes.random(16).array(), mode);
            hashids = new Hashids(Bytes.random(16).encodeBase64());
        }
    }

    @Benchmark
    public void benchmarkIdMask(MyState state, Blackhole blackhole) {
        blackhole.consume(state.idMaskEngine.mask(Bytes.from(state.id).array()));
        state.id++;
    }

    @Benchmark
    public void benchmarkHashId(MyState state, Blackhole blackhole) {
        blackhole.consume(state.hashids.encode(state.id));
        state.id++;
    }
}
