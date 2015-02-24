package webster.decisions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class Completion implements Node {
    private final static Logger logger = LoggerFactory.getLogger(Completion.class);

    private final String name;
    private final BiFunction<Resource, Request, CompletableFuture<Response>> completion;

    public Completion(String name, BiFunction<Resource, Request, CompletableFuture<Response>> completion) {
        this.name = name;
        this.completion = completion;
    }

    @Override
    public CompletableFuture<Response> apply(Resource resource, Request request) {
        return completion.apply(resource, request).thenApply(response -> {
            logger.info(name + " completion -> " + response.status());
            return response;
        });
    }

    public static interface Fn extends BiFunction<Resource, Request, CompletableFuture<Response>> {
    }
}
