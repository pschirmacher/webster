package webster.requestresponse;

import java.io.InputStream;

public class InputStreamResponseBody implements ResponseBody {
    private final InputStream content;

    public InputStreamResponseBody(final InputStream content){
        this.content = content;
    }

    public InputStream content(){
        return content;
    }

    @Override
    public <T> T process(ResponseBodyProcessor<T> processor) {
        return processor.process(this);
    }
}
