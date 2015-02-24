package webster.requestresponse;

public class StringResponseBody implements ResponseBody{
    private final String content;

    public StringResponseBody(final String content){
        this.content = content;
    }

    public String content(){
        return content;
    }

    @Override
    public <T> T process(ResponseBodyProcessor<T> processor) {
        return processor.process(this);
    }
}
