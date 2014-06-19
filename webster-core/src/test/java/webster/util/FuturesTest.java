package webster.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;

public class FuturesTest {

    @Test
    public void afterTimeout() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Set<String> resultHolder = Collections.newSetFromMap(new ConcurrentHashMap<>());
        CompletableFuture<String> slow = Futures.afterTimeout("slow", 1000);
        CompletableFuture<String> fast = Futures.afterTimeout("fast", 1);
        fast.acceptEither(slow, result -> {
            resultHolder.add(result);
            latch.countDown();
        });
        latch.await();
        Assert.assertEquals("fast", resultHolder.iterator().next());
    }
}
