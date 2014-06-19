package webster.links;

import webster.util.Maps;

import java.util.*;
import java.util.stream.Collectors;

public interface ExpandableLink {

    String scheme();

    String host();

    Optional<Integer> port();

    Optional<String> context();

    String pattern();

    String routingPattern();

    String absoluteUrl();

    String relativeUrl();

    ExpandedLink expandAll(Map<String, ? extends Object> pathParams);

    default ExpandedLink expand(String name, Object value) {
        return expandAll(Maps.newStringMap().with(name, String.valueOf(value)).build());
    }

    default ExpandedLink withQueryParams(Map<String, ? extends Object> queryParams) {
        return withQueryParamsList(queryParams.entrySet().stream()
                .map(e -> Maps.entry(e.getKey(), Arrays.asList(String.valueOf(e.getValue()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    default ExpandedLink withQueryParam(String name, Object value) {
        return withQueryParams(Maps.newStringMap().with(name, String.valueOf(value)).build());
    }

    ExpandedLink withQueryParamsList(Map<String, List<? extends Object>> queryParams);
}
