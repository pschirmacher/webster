package webster.resource;

import spark.utils.MimeParse;
import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ContentNegotiation {

    static Optional<String> bestMediaTypeFor(Request request, Set<String> supportedMediaTypes) {
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1:
        // "If no Accept header field is present, then it is assumed that the client accepts all media types."
        String requestedMediaType = request.header("Accept").value().orElse("*/*");
        try {
            String bestMatch = MimeParse.bestMatch(supportedMediaTypes, requestedMediaType);
            return MimeParse.NO_MIME_TYPE.equals(bestMatch)
                    ? Optional.empty()
                    : Optional.of(bestMatch);
        } catch (RuntimeException e) {
            // spark throws exception on invalid accept header
            return Optional.empty();
        }
    }

    default CompletableFuture<ResponseBody> contentNegotiationEntity(Request request) {
        String bestMatch = bestMediaTypeFor(request, contentNegotation().mediaTypes()).orElseThrow(IllegalStateException::new);
        return contentNegotation().producerFor(bestMatch).apply(request);
    }

    default ContentNegotiator supportFor() {
        return new ContentNegotiator();
    }

    ContentNegotiator contentNegotation();

    public static class ContentNegotiator {

        private final Map<String, Function<Request, CompletableFuture<ResponseBody>>> mediaTypeProducers = new HashMap<>();

        public Set<String> mediaTypes() {
            return mediaTypeProducers.keySet();
        }

        public Function<Request, CompletableFuture<ResponseBody>> producerFor(String mediaType) {
            return mediaTypeProducers.get(mediaType);
        }

        public ContentNegotiator mediaType(String mediaType, Function<Request, CompletableFuture<ResponseBody>> producer) {
            mediaTypeProducers.put(mediaType, producer);
            return this;
        }
    }
}
