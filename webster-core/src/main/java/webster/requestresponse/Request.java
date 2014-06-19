package webster.requestresponse;

import webster.util.Maps;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Request {

    private final String method;
    private final String uri;
    private final Map<String, String> headers;
    private final InputStream body;
    private final Map<String, List<String>> requestParams;
    private final List<String> splats;
    private final Map<String, String> pathParams;

    private volatile Scope context;
    private volatile Scope flash;
    private volatile Scope session;
    private volatile boolean bodyRead;
    private volatile Optional<String> bodyString;

    public Request(String method, String uri, Map<String, String> headers, InputStream body,
                   Map<String, List<String>> requestParams, List<String> splats, Map<String, String> pathParams) {
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.body = body;
        this.requestParams = requestParams;
        this.splats = splats;
        this.pathParams = pathParams;
        this.context = new Scope();
        this.flash = new Scope();
        this.session = new Scope();
        this.bodyRead = false;
        this.bodyString = null;
    }

    private Request(String method, String uri, Map<String, String> headers, InputStream body,
                    Map<String, List<String>> requestParams, List<String> splats, Map<String, String> pathParams,
                    Scope context, Scope flash, Scope session, boolean bodyRead, Optional<String> bodyString) {
        this(method, uri, headers, body, requestParams, splats, pathParams);
        this.context = context;
        this.flash = flash;
        this.session = session;
        this.bodyRead = bodyRead;
        this.bodyString = bodyString;
    }

    public String method() {
        return method;
    }

    public String uri() {
        return uri;
    }

    public ValueSupplier<Map<String, String>> headers() {
        return new ValueSupplier<>(headers);
    }

    public ValueSupplier<Optional<String>> header(String name) {
        return new ValueSupplier<>(Optional.ofNullable(headers.get(name)));
    }

    public ValueSupplier<Optional<String>> body() {
        return new ValueSupplier<>(bodyAsString());
    }

    public ValueSupplier<InputStream> bodyStream() {
        return new ValueSupplier<>(body);
    }

    public ValueSupplier<Map<String, List<String>>> multiParams() {
        return new ValueSupplier<>(requestParams);
    }

    public ValueSupplier<Map<String, String>> params() {
        Map<String, String> firstValues = requestParams.entrySet().stream()
                .map(entry -> Maps.entry(entry.getKey(), entry.getValue().get(0)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ValueSupplier<>(firstValues);
    }

    public ValueSupplier<List<String>> paramValues(String name) {
        return new ValueSupplier<>(requestParams.containsKey(name)
                ? requestParams.get(name)
                : Collections.emptyList());
    }

    public ValueSupplier<Optional<String>> param(String name) {
        List<String> paramValues = paramValues(name).value();
        return new ValueSupplier<>(paramValues.isEmpty()
                ? Optional.empty()
                : Optional.of(paramValues.get(0)));
    }

    public ValueSupplier<Map<String, String>> pathParams() {
        return new ValueSupplier<>(pathParams);
    }

    public ValueSupplier<Optional<String>> pathParam(String name) {
        return new ValueSupplier<>(Optional.ofNullable(pathParams.get(name)));
    }

    public ValueSupplier<List<String>> splats() {
        return new ValueSupplier<>(splats);
    }

    public Scope context() {
        return context;
    }

    public Scope flash() {
        return flash;
    }

    public Scope session() {
        return session;
    }

    private synchronized Optional<String> bodyAsString() {
        if (!bodyRead) {
            bodyRead = true;
            // TODO charset?
            Scanner scanner = new Scanner(body, "UTF-8").useDelimiter("\\A");
            bodyString = scanner.hasNext()
                    ? Optional.of(scanner.next())
                    : Optional.empty();
        }
        return bodyString;
    }

    public Request withMethod(String method) {
        return new Request(method, uri, headers, body, requestParams, splats, pathParams, context, flash, session,
                bodyRead, bodyString);
    }

    public Request withSplatsAndPathParams(List<String> splats, Map<String, String> pathParams) {
        return new Request(method, uri, headers, body, requestParams, splats, pathParams, context, flash, session,
                bodyRead, bodyString);
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", headers=" + headers +
                ", requestParams=" + requestParams +
                ", splats=" + splats +
                ", pathParams=" + pathParams +
                ", body=" + body +
                '}';
    }
}
