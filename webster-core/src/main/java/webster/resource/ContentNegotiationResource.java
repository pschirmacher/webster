package webster.resource;

import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class ContentNegotiationResource implements Resource, ContentNegotiation {

    @Override
    public CompletableFuture<ResponseBody> entity(Request request) {
        return contentNegotiationEntity(request);
    }

    @Override
    public Set<String> supportedMediaTypes() {
        return contentNegotation().mediaTypes();
    }
}