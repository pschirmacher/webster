package webster.routing;

import webster.decisions.DefaultFlow;
import webster.links.ExpandableLink;
import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class RoutingBuilder {

    public static RoutingTableBuilder routingTable() {
        return new RoutingTableBuilder();
    }

    public static RouteToResourceBuilder from(String pattern) {
        return new RouteToResourceBuilder(pattern);
    }

    public static RouteToResourceBuilder from(ExpandableLink link) {
        return from(link.routingPattern());
    }

    public static class RouteToResourceBuilder {

        private final String pattern;

        private BiFunction<Resource, Request, CompletableFuture<Response>> decisionFlow = DefaultFlow.get();

        public RouteToResourceBuilder(String pattern) {
            this.pattern = pattern;
        }

        public RouteToResourceBuilder using(BiFunction<Resource, Request, CompletableFuture<Response>> decisionFlow) {
            this.decisionFlow = decisionFlow;
            return this;
        }

        public Route toResource(Supplier<Resource> resource) {
            return new Route(pattern, request -> decisionFlow.apply(resource.get(), request));
        }

        public Route toResource(Resource singleton) {
            return toResource(() -> singleton);
        }
    }

    public static class RoutingTableBuilder {

        private final List<Route> routes = new ArrayList<>();

        public RoutingTableBuilder withRoute(Route route) {
            routes.add(route);
            return this;
        }

        public RoutingTable build() {
            return new RoutingTable(routes);
        }
    }
}
