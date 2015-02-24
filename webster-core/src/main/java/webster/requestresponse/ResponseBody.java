package webster.requestresponse;

public interface ResponseBody {
    <T> T process(ResponseBodyProcessor<T> processor);
}
