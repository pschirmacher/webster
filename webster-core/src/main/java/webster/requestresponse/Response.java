package webster.requestresponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Response {

    private final int status;
    private final Object body;
    private final Map<String, String> headers;

    public Response(Throwable throwable) {
        this(500, "internal server error");
        throwable.printStackTrace(); // TODO proper logging
    }

    public Response(int status) {
        this(status, null, new HashMap<>());
    }

    public Response(int status, Object body) {
        this(status, body, new HashMap<>());
    }

    public Response(int status, Map<String, String> headers) {
        this(status, null, headers);
    }

    public Response(int status, Object body, Map<String, String> headers) {
        Objects.requireNonNull(headers);
        this.headers = headers;
        this.status = status;
        this.body = body;
    }

    public int status() {
        return status;
    }

    public Object body() {
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

    public Response withBody(Object body) {
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
        int maxLen = 100;
        if (body == null) {
            return "null";
        } else if (body instanceof String) {
            return body.toString().length() >= maxLen ? body.toString().substring(0, maxLen) + "..." : body.toString();
        } else {
            return body.toString();
        }
    }
}
