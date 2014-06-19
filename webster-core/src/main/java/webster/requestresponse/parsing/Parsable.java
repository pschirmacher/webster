package webster.requestresponse.parsing;

import java.util.function.Function;

public interface Parsable<I> {

    I value();

    default <O> O parse(Function<I, O> parser) {
        return parser.apply(value());
    }
}
