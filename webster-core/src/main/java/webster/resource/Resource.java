package webster.resource;

import webster.requestresponse.Request;
import webster.requestresponse.Response;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface Resource {

    default CompletableFuture<Response> override(Request request, Response response) {
        return CompletableFuture.completedFuture(response);
    }

    default CompletableFuture<Optional<Instant>> lastModified(Request request) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    default CompletableFuture<Optional<Instant>> expires(Request request) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    default CompletableFuture<Optional<String>> etag(Request request) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    default CompletableFuture<Void> onDelete(Request request) {
        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<Void> onPost(Request request) {
        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<Void> onPut(Request request) {
        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<Boolean> isMalformed(Request request) {
        // TODO validate Accept header
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<String> badRequestEntity(Request request) {
        return CompletableFuture.completedFuture("");
    }

    default CompletableFuture<Boolean> isAuthorized(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isAllowed(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isPostAllowed(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isPostValid(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isPutAllowed(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isPutValid(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Boolean> isDeletetAllowed(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<String> locationHeader(Request request) {
        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<Map<String, String>> additionalHeaders(int responseStatus, Request request) {
        return CompletableFuture.completedFuture(new HashMap<>());
    }

    default CompletableFuture<Boolean> createdNewResource(Request request) {
        return CompletableFuture.completedFuture("POST".equalsIgnoreCase(request.method()));
    }

    default CompletableFuture<Boolean> resourcePreviouslyExisted(Request request) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> resourceMovedPermanently(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default CompletableFuture<Set<String>> supportedMediaTypes(Request request) {
        return CompletableFuture.completedFuture(supportedMediaTypes());
    }

    default CompletableFuture<Boolean> redirectAfterPost(Request request) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> isPostToMissingResourceAllowed(Request request) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> isConflict(Request request) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> respondWithEntity(Request request) {
        return CompletableFuture.completedFuture(true);
    }

    default Set<String> supportedContentTypes() {
        return null;
    }

    default Set<String> supportedMediaTypes() {
        return Collections.singleton("text/html");
    }

    default Set<String> allowedMethods() {
        return Collections.singleton("GET");
    }

    default Set<String> knownMethods() {
        // TODO patch? trace?
        return new HashSet<>(Arrays.asList("GET", "PUT", "POST", "DELETE", "HEAD", "OPTIONS"));
    }

    CompletableFuture<Boolean> doesRequestedResourceExist(Request request);

    CompletableFuture<Object> entity(Request request);
}
