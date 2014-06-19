package webster.links;

import java.util.*;
import java.util.stream.Collectors;

// TODO url encoding
public class ExpandedLink implements ExpandableLink {
    private final Link link;
    private final Map<String, Object> pathParams;
    private final Map<String, List<Object>> queryParams;

    public ExpandedLink(Link link, Map<String, Object> pathParams, Map<String, List<? extends Object>> queryParams) {
        this.link = link;
        this.pathParams = pathParams;
        this.queryParams = new HashMap<>();
        queryParams.entrySet().stream().forEach(e -> this.queryParams.put(e.getKey(), (List<Object>) e.getValue()));
    }

    public String relativeUrl() {
        return addQueryParams(mergePathParams(link.relativeUrl(), pathParams), queryParams);
    }

    @Override
    public String scheme() {
        return link.scheme();
    }

    @Override
    public String host() {
        return link.host();
    }

    @Override
    public Optional<Integer> port() {
        return link.port();
    }

    @Override
    public Optional<String> context() {
        return link.context();
    }

    @Override
    public String pattern() {
        return link.pattern();
    }

    @Override
    public String routingPattern() {
        return link.routingPattern();
    }

    @Override
    public String absoluteUrl() {
        return addQueryParams(mergePathParams(link.absoluteUrl(), pathParams), queryParams);
    }

    private String addQueryParams(String url, Map<String, List<Object>> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (!queryParams.isEmpty()) {
            urlBuilder.append("?");
        }
        for (Map.Entry<String, List<Object>> queryParam : queryParams.entrySet()) {
            String params = queryParam.getValue().stream()
                    .map(value -> queryParam.getKey() + "=" + value)
                    .collect(Collectors.joining("&"));
            if (urlBuilder.toString().endsWith("?")) {
                urlBuilder.append(params);
            } else {
                urlBuilder.append("&" + params);
            }
        }
        return urlBuilder.toString();
    }

    @Override
    public ExpandedLink expandAll(Map<String, ? extends Object> pathParams) {
        this.pathParams.putAll(pathParams);
        return this;
    }

    @Override
    public ExpandedLink withQueryParamsList(Map<String, List<? extends Object>> queryParams) {
        queryParams.entrySet().stream().forEach(e -> this.queryParams.put(e.getKey(), (List<Object>) e.getValue()));
        return this;
    }

    private String mergePathParams(String url, Map<String, Object> pathParams) {
        Comparator<Map.Entry<String, ? extends Object>> comparing = Comparator
                .comparing(e -> e.getKey(), Comparator.comparingInt(s -> s.toString().length()));
        TreeSet<Map.Entry<String, ? extends Object>> byLengthDesc = new TreeSet<>(comparing.reversed());
        byLengthDesc.addAll(pathParams.entrySet());
        // longer path params first so that :foo123 is replaced before :foo12
        return byLengthDesc.stream().reduce(
                url,
                (s, e) -> {
                    String paramName = e.getKey().startsWith(":") ? e.getKey() : ":" + e.getKey();
                    return s.replaceAll(paramName, String.valueOf(e.getValue()));
                },
                (s1, s2) -> s1 + s2);
    }
}
