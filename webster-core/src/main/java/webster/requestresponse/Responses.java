package webster.requestresponse;

import java.io.InputStream;

public final class Responses {
    private static final ResponseBody singleInstance = new EmptyResponseBody();

    public static ResponseBody emptyBody(){
        return singleInstance;
    }

    public static ResponseBody bodyFrom(String content){
        return new StringResponseBody(content);
    }

    public static ResponseBody bodyFrom(InputStream content){
        return new InputStreamResponseBody(content);
    }
}
