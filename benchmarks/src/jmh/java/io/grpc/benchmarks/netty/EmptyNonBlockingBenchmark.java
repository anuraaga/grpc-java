package io.grpc.benchmarks.netty;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.benchmarks.proto.BenchmarkServiceGrpc;
import io.grpc.benchmarks.proto.BenchmarkServiceGrpc.BenchmarkServiceFutureStub;
import io.grpc.benchmarks.proto.BenchmarkServiceGrpc.BenchmarkServiceImplBase;
import io.grpc.benchmarks.proto.Messages.SimpleRequest;
import io.grpc.benchmarks.proto.Messages.SimpleResponse;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class EmptyNonBlockingBenchmark {

  private static class EmptyBenchmarkService extends BenchmarkServiceImplBase {

    @Override
    public void unaryCall(SimpleRequest request, StreamObserver<SimpleResponse> responseObserver) {
      responseObserver.onNext(SimpleResponse.getDefaultInstance());
      responseObserver.onCompleted();
    }
  }

  @AuxCounters
  @State(Scope.Thread)
  public static class Counters {
    AtomicLong successes = new AtomicLong();
    AtomicLong failures = new AtomicLong();

    public long successes() {
      return successes.get();
    }

    public long failures() {
      return this.failures.get();
    }

    @Setup(Level.Iteration)
    public void reset() {
      successes.set(0);
      failures.set(0);
    }
  }

  private Server server;
  private ManagedChannel channel;

  private BenchmarkServiceFutureStub futureStub;

  @Setup
  public void start() throws Exception {
    server = ServerBuilder.forPort(0)
        .addService(new EmptyBenchmarkService())
        .directExecutor()
        .build();
    server.start();
    channel = ManagedChannelBuilder.forAddress("127.0.0.1", server.getPort())
        .directExecutor()
        .usePlaintext(true)
        .build();
    futureStub = BenchmarkServiceGrpc.newFutureStub(channel);
  }

  @TearDown
  public void stop() throws Exception {
    channel.shutdownNow().awaitTermination(10, TimeUnit.SECONDS);
    server.shutdownNow().awaitTermination();
  }

  @Benchmark
  public void emptyNonBlocking(final Counters counters) throws Exception {
    Futures.addCallback(
        futureStub.unaryCall(SimpleRequest.getDefaultInstance()),
        new FutureCallback<SimpleResponse>() {
          @Override
          public void onSuccess(@Nullable SimpleResponse result) {
            counters.successes.incrementAndGet();
          }

          @Override
          public void onFailure(Throwable t) {
            counters.failures.incrementAndGet();
          }
        },
        MoreExecutors.directExecutor());
  }
}
