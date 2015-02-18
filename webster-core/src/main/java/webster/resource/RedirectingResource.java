package webster.resource;

import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;
import webster.requestresponse.Responses;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface RedirectingResource extends Resource {

    @Override
    default CompletableFuture<ResponseBody> entity(Request request) {
        return CompletableFuture.completedFuture(Responses.emptyBody());
    }

    @Override
    default CompletableFuture<Boolean> doesRequestedResourceExist(Request request) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    default Set<String> supportedMediaTypes() {
        return Collections.singleton("*/*");
    }

    @Override
    default CompletableFuture<Boolean> resourcePreviouslyExisted(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    CompletableFuture<String> locationHeader(Request request);
}
