package webster.requestresponse.parsing;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MultiValueFormParser implements Function<Optional<String>, Map<String, List<String>>> {

    @Override
    public Map<String, List<String>> apply(Optional<String> in) {
        // TODO charset
        return in.isPresent()
                ? new QueryStringDecoder(in.get(), false).parameters()
                : Collections.emptyMap();
    }
}
