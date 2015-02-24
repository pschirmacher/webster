package webster.decisions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class Decision implements Node {
    private final static Logger logger = LoggerFactory.getLogger(Decision.class);

    private final String name;
    private final BiFunction<Resource, Request, CompletableFuture<Boolean>> decision;
    private final Node onTrue;
    private final Node onFalse;

    public Decision(String name, BiFunction<Resource, Request, CompletableFuture<Boolean>> decision, Node onTrue, Node onFalse) {
        this.name = name;
        this.decision = decision;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public CompletableFuture<Response> apply(Resource resource, Request request) {
        return decision
                .apply(resource, request)
                .thenCompose(decisionResult -> {
                    logger.debug("made " + name + " decision -> " + decisionResult);
                    return decisionResult
                            ? onTrue.apply(resource, request)
                            : onFalse.apply(resource, request);
                });
    }

    public static interface Fn extends BiFunction<Resource, Request, CompletableFuture<Boolean>> {
    }
}
