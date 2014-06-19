package webster.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Maps {

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    public static <K, V> Builder<K, V> newMap() {
        return new Builder<>();
    }

    public static Builder<String, String> newStringMap() {
        return new Builder<>();
    }

    public static class Builder<K, V> {

        private final Map<K, V> map = new HashMap<>();

        public Builder<K, V> with(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Builder<K, V> withIfNotNull(K key, V value) {
            if (value != null) {
                map.put(key, value);
            }
            return this;
        }

        public Map<K, V> build() {
            return map;
        }
    }
}
