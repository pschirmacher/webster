package webster.routing;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface Decoratable<T, R> extends Function<T, R> {

    default Decoratable<T, R> decoratedWith(UnaryOperator<Function<T, R>> decorator) {
        Function<T, R> decorated = decorator.apply(this);
        return decorated::apply;
    }
}
