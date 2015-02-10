package webster.requestresponse;

import java.io.InputStream;

public final class Responses {
    private static final EmptyResponseBody singleInstance = new EmptyResponseBody();

    public static EmptyResponseBody empty(){
        return singleInstance;
    }

    public static ResponseBody from(String content){
        return new StringResponseBody(content);
    }

    public static ResponseBody from(InputStream content){
        return new InputStreamResponseBody(content);
    }
}
