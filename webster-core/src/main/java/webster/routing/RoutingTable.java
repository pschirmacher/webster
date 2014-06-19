package webster.routing;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.util.Maps;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RoutingTable implements Decoratable<Request, CompletableFuture<Response>> {

    private final List<Route> routes;

    public RoutingTable(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    public CompletableFuture<Response> apply(Request request) {
        Optional<Route> route = routes.stream().filter(r -> r.matches(request)).findFirst();
        return route.isPresent()
                ? route.get().apply(request)
                : CompletableFuture.completedFuture(new Response(404, "not found", Maps.newStringMap().with("Content-Type", "text/plain").build()));
    }
}
