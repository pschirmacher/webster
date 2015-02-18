package webster.resource;

import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;
import webster.requestresponse.Responses;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class HtmlResource implements Resource, Html {

    @Override
    public Set<String> supportedMediaTypes() {
        return Collections.singleton("text/html");
    }

    @Override
    public CompletableFuture<ResponseBody> entity(Request request) {
        return templateModel(request)
                .thenApply(model -> Responses.bodyFrom(renderHtml(template(), model, request)));
    }

    public abstract String template();

    public abstract CompletableFuture<Map<String, Object>> templateModel(Request request);
}
