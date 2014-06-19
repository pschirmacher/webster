package webster.links;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// TODO url encoding
// TODO validation (e.g. slashes)
public class Link implements ExpandableLink {

    private final String scheme;
    private final String host;
    private final Optional<Integer> port;
    private final Optional<String> context;
    private final String pattern;

    public Link(String scheme, String host, Optional<Integer> port, Optional<String> context, String pattern) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
        this.pattern = pattern;
    }

    @Override
    public String scheme() {
        return scheme;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public Optional<Integer> port() {
        return port;
    }

    @Override
    public Optional<String> context() {
        return context;
    }

    @Override
    public String pattern() {
        return pattern;
    }

    @Override
    public String routingPattern() {
        return relativeUrl();
    }

    @Override
    public String absoluteUrl() {
        String hostWithPort = port.isPresent() ? host + ":" + port.get() : host;
        return scheme + "://" + hostWithPort + relativeUrl();
    }

    @Override
    public String relativeUrl() {
        return context.isPresent()
                ? "/" + context.get() + pattern
                : pattern;
    }

    @Override
    public ExpandedLink expandAll(Map<String, ? extends Object> pathParams) {
        return new ExpandedLink(this, (Map<String, Object>) pathParams, new HashMap<>());
    }

    @Override
    public ExpandedLink withQueryParamsList(Map<String, List<? extends Object>> queryParams) {
        return new ExpandedLink(this, new HashMap<>(), queryParams);
    }

    public static class Builder {
        private String scheme = "http";
        private String host = "localhost";
        private Optional<Integer> port = Optional.empty();
        private Optional<String> context = Optional.empty();
        private String pattern = "/*";

        public Builder withScheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(Integer port) {
            this.port = Optional.ofNullable(port);
            return this;
        }

        public Builder withContext(String context) {
            this.context = Optional.ofNullable(context);
            return this;
        }

        public Builder withPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder withPath(String path) {
            this.pattern = path;
            return this;
        }

        public Link build() {
            return new Link(scheme, host, port, context, pattern);
        }
    }
}
