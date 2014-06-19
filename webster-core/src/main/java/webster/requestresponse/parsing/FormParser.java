package webster.requestresponse.parsing;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class FormParser implements Function<Optional<String>, Map<String, String>> {

    @Override
    public Map<String, String> apply(Optional<String> in) {
        // TODO charset
        return in.isPresent()
                ? new QueryStringDecoder(in.get(), false).parameters().entrySet().stream().collect(
                toMap(entry -> entry.getKey(), entry -> entry.getValue().get(0)))
                : Collections.emptyMap();
    }
}
