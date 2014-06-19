package webster.decisions;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface Node extends BiFunction<Resource, Request, CompletableFuture<Response>> {
}
