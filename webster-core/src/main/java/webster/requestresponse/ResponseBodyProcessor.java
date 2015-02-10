package webster.requestresponse;

public interface ResponseBodyProcessor<T> {
    T process(StringResponseBody body);

    T process(InputStreamResponseBody body);

    T process(EmptyResponseBody body);
}
