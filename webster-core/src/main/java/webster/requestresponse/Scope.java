package webster.requestresponse;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Scope {

    private final Map<String, Object> items = new ConcurrentHashMap<>();

    public <T> T put(String key, Object item) {
        return (T) items.put(key, item);
    }

    public <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) items.get(key));
    }

    public <T> T getExisting(String key) {
        return (T) items.get(key);
    }

    public Set<String> keys() {
        return items.keySet();
    }

    public <T> T accept(Function<Scope, T> visitor) {
        return visitor.apply(this);
    }
}
