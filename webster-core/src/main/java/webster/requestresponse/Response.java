package webster.requestresponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Response {
    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    private final int status;
    private final ResponseBody body;
    private final Map<String, String> headers;

    public Response(Throwable throwable) {
        this(500, new StringResponseBody("internal server error"));
        logger.warn(throwable.getMessage(), throwable);
    }

    public Response(int status) {
        this(status, null, new HashMap<>());
    }

    public Response(int status, ResponseBody body) {
        this(status, body, new HashMap<>());
    }

    public Response(int status, Map<String, String> headers) {
        this(status, null, headers);
    }

    public Response(int status, ResponseBody body, Map<String, String> headers) {
        Objects.requireNonNull(headers);
        this.headers = headers;
        this.status = status;
        this.body = body;
    }

    public int status() {
        return status;
    }

    public ResponseBody body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Response withAdditionalHeaders(Map<String, String> additionalHeaders) {
        headers.putAll(additionalHeaders);
        return this;
    }

    public Response withAdditionalHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public Response withStatus(int status) {
        return new Response(status, body, headers);
    }

    public Response withBody(ResponseBody body) {
        return new Response(status, body, headers);
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", body=" + bodyToString() +
                ", headers=" + headers +
                '}';
    }

    private String bodyToString() {
        return body().process(Processors.stringProcessor(100));
    }
}
