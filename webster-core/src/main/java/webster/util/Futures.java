package webster.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Futures {

    private static final Timer timer = new HashedWheelTimer();

    public static <T> CompletableFuture<T> afterTimeout(T value, long millis) {
        CompletableFuture<T> future = new CompletableFuture<>();
        timer.newTimeout(t -> future.complete(value), millis, TimeUnit.MILLISECONDS);
        return future;
    }
}
