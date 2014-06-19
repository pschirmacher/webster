package webster.requestresponse;

import webster.requestresponse.parsing.Parsable;

public class ValueSupplier<T> implements Parsable<T> {

    private final T value;

    public ValueSupplier(T value) {
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }
}
