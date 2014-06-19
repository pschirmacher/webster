package webster.routing;

import spark.route.HttpMethod;
import spark.route.RouteMatch;
import spark.route.RouteMatcher;
import spark.route.SimpleRouteMatcher;
import spark.utils.SparkUtils;
import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.spark.Spark;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class Route implements Decoratable<Request, CompletableFuture<Response>> {

    private final Decoratable<Request, CompletableFuture<Response>> handler;
    private final RouteMatcher routeMatcher;
    private final String pattern;

    public Route(String pattern, Decoratable<Request, CompletableFuture<Response>> handler) {
        this.handler = handler;
        this.routeMatcher = routeMatcherFor(pattern);
        this.pattern = pattern;
    }

    private RouteMatcher routeMatcherFor(String pattern) {
        RouteMatcher matcher = new SimpleRouteMatcher();
        matcher.parseValidateAddRoute(HttpMethod.get + " '" + pattern + "'", "*/*", null);
        return matcher;
    }

    private Optional<RouteMatch> matchFor(Request request) {
        return Optional.ofNullable(routeMatcher.findTargetForRequestedRoute(HttpMethod.get, request.uri(), "*/*"));
    }

    public boolean matches(Request request) {
        return matchFor(request).isPresent();
    }

    @Override
    public CompletableFuture<Response> apply(Request request) {
        Optional<RouteMatch> routeMatch = matchFor(request);
        if (!routeMatch.isPresent()) {
            throw new IllegalArgumentException("route doesn't match");
        } else {
            return handler.apply(requestWithPathParams(request, routeMatch.get()));
        }
    }

    private Request requestWithPathParams(Request request, RouteMatch routeMatch) {
        List<String> requestList = SparkUtils.convertRouteToList(routeMatch.getRequestURI());
        List<String> matchList = SparkUtils.convertRouteToList(routeMatch.getMatchUri());
        Map<String, String> pathParams = Spark.getParams(requestList, matchList);
        List<String> splats = Spark.getSplat(requestList, matchList);
        return request.withSplatsAndPathParams(splats, pathParams);
    }

    @Override
    public Route decoratedWith(UnaryOperator<Function<Request, CompletableFuture<Response>>> decorator) {
        return new Route(pattern, handler.decoratedWith(decorator));
    }
}
