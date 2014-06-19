package webster.decisions;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class Action implements Node {

    private final String name;
    private final BiFunction<Resource, Request, CompletableFuture<Void>> action;
    private final Node then;

    public Action(String name, BiFunction<Resource, Request, CompletableFuture<Void>> action, Node then) {
        this.name = name;
        this.action = action;
        this.then = then;
    }

    @Override
    public CompletableFuture<Response> apply(Resource resource, Request request) {
        System.out.println("performing " + name + " action");
        return action.apply(resource, request)
                .thenCompose(ignored -> then.apply(resource, request));
    }

    public static interface Fn extends BiFunction<Resource, Request, CompletableFuture<Void>> {
    }
}
