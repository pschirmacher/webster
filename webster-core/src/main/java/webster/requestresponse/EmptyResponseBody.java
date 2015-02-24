package webster.requestresponse;

public class EmptyResponseBody implements ResponseBody{
    private static final EmptyResponseBody singleInstance = new EmptyResponseBody();

    public static EmptyResponseBody empty(){
        return singleInstance;
    }

    @Override
    public <T> T process(ResponseBodyProcessor<T> processor) {
        return processor.process(this);
    }
}
