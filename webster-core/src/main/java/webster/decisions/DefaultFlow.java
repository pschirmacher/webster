package webster.decisions;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.requestresponse.parsing.Parsable;
import webster.util.Maps;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static webster.decisions.NodeBuilder.*;
import static webster.requestresponse.parsing.Parsers.*;
import static webster.resource.ContentNegotiation.bestMediaTypeFor;
import static webster.requestresponse.Responses.from;

public class DefaultFlow {

    public static final Collection<Integer> etagStatuses = Arrays.asList(200, 206, 304);

    public static Response withContentType(Response resp, Request req, Set<String> mediaTypes) {
        return resp.withAdditionalHeader("Content-Type", bestMediaTypeFor(req, mediaTypes).orElse("text/plain"));
    }

    public static <T> Function<Response, CompletionStage<Response>> withOptionalHeader(
            String header,
            Function<Response, CompletionStage<Optional<T>>> value,
            Function<T, String> format) {
        return resp ->
                value.apply(resp).thenApply(v -> v.isPresent()
                        ? resp.withAdditionalHeader(header, format.apply(v.get()))
                        : resp);
    }

    public static Completion.Fn completionWithDefaults(Completion.Fn c) {
        return (r, req) -> {
            Function<Response, CompletionStage<Response>> setContentType = resp ->
                    resp.body() == null
                            ? CompletableFuture.completedFuture(resp)
                            : r.supportedMediaTypes(req).thenApply(mediaTypes -> withContentType(resp, req, mediaTypes));
            Function<Response, CompletionStage<Response>> setLastModified = withOptionalHeader(
                    "Last-Modified",
                    resp -> r.lastModified(req),
                    lastModified -> rfc1123().format(new Date(lastModified.toEpochMilli())));
            Function<Response, CompletionStage<Response>> setExpires = withOptionalHeader(
                    "Expires",
                    resp -> r.expires(req),
                    expires -> rfc1123().format(new Date(expires.toEpochMilli())));
            Function<Response, CompletionStage<Response>> setETag = withOptionalHeader(
                    "ETag",
                    resp -> etagStatuses.contains(resp.status())
                            ? r.etag(req)
                            : CompletableFuture.completedFuture(Optional.empty()),
                    identity()
            );
            return c.apply(r, req)
                    .thenCompose(setContentType)
                    .thenCompose(setLastModified)
                    .thenCompose(setExpires)
                    .thenCompose(setETag)
                    .thenApply(resp -> resp.withAdditionalHeader("Date", rfc1123().format(new Date())))
                    .thenCompose(resp -> r.additionalHeaders(resp.status(), req).thenApply(resp::withAdditionalHeaders))
                    .thenCompose(resp -> r.override(req, resp));
        };
    }

    public static Completion.Fn completionWithLocationAndStatus(int status) {
        return completionWithDefaults((r, req) ->
                r.locationHeader(req)
                        .thenApply(location -> location != null
                                ? Maps.newStringMap().with("Location", location).build()
                                : Maps.newStringMap().build())
                        .thenApply(headers -> new Response(status, headers)));
    }

    public static Completion.Fn notYetImplemented = (r, req) ->
            CompletableFuture.completedFuture(new Response(500, from("--- NOT YET IMPLEMENTED ---")));

    public static Completion.Fn created = completionWithLocationAndStatus(201);

    public static Completion.Fn noContent = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(204)));

    public static Decision.Fn isServiceAvailable = (r, req) ->
            CompletableFuture.completedFuture(true);

    public static Decision.Fn isKnownMethod = (r, req) ->
            CompletableFuture.completedFuture(r.knownMethods().contains(req.method()));

    public static Completion.Fn serviceNotAvailable = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(503)));

    public static Completion.Fn unknownMethod = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(501)));

    public static Decision.Fn isMethodAllowed = (r, req) ->
            CompletableFuture.completedFuture(r.allowedMethods().contains(req.method().toUpperCase()));

    public static Completion.Fn methodNotAllowed = completionWithDefaults((r, req) -> {
        String allow = r.allowedMethods().stream().collect(Collectors.joining(", "));
        Map<String, String> headers = new HashMap<>();
        headers.put("Allow", allow);
        return CompletableFuture.completedFuture(new Response(405, headers));
    });

    public static Decision.Fn unknownContentType = (r, req) -> {
        Set<String> knownTypes = r.supportedContentTypes();
        Optional<String> unknownType = req.header("Content-Type").value()
                .filter(ct -> knownTypes != null && !knownTypes.contains(ct));
        return CompletableFuture.completedFuture(unknownType.isPresent());
    };

    public static Completion.Fn unsupportedMediaType = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(415)));

    public static Decision.Fn isMalformed = (r, req) ->
            r.isMalformed(req);

    public static Completion.Fn badRequest = completionWithDefaults((r, req) ->
            r.badRequestEntity(req).thenApply(e -> new Response(400, from(e))));

    public static Action.Fn doDelete = (r, req) -> r.onDelete(req);

    public static Action.Fn doPost = (r, req) -> r.onPost(req);

    public static Action.Fn doPut = (r, req) -> r.onPut(req);

    public static Decision.Fn isDeleteEnacted = (r, req) ->
            CompletableFuture.completedFuture(true);

    public static Decision.Fn isRespondWithEntity = (r, req) ->
            r.respondWithEntity(req);

    public static Completion.Fn ok = completionWithDefaults((r, req) ->
            r.entity(req).thenApply(e -> new Response(200, e)));

    public static Decision.Fn isUnauthorized = (r, req) ->
            r.isAuthorized(req).thenApply(a -> !a);

    public static Completion.Fn unauthorized = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(401)));

    public static Decision.Fn isForbidden = (r, req) ->
            r.isAllowed(req).thenApply(a -> !a);

    public static Completion.Fn forbidden = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(403)));

    public static Completion.Fn options = completionWithDefaults((r, req) ->
            // TODO options
            CompletableFuture.completedFuture(new Response(200)));

    public static Decision.Fn isExistingResource = (r, req) ->
            r.doesRequestedResourceExist(req);

    public static Decision.Fn isMethodOptions = (r, req) ->
            CompletableFuture.completedFuture("OPTIONS".equalsIgnoreCase(req.method()));

    public static Decision.Fn isMethodPost = (r, req) ->
            CompletableFuture.completedFuture("POST".equalsIgnoreCase(req.method()));

    public static Decision.Fn isMethodPut = (r, req) ->
            CompletableFuture.completedFuture("PUT".equalsIgnoreCase(req.method()));

    public static Decision.Fn isMethodDelete = (r, req) ->
            CompletableFuture.completedFuture("DELETE".equalsIgnoreCase(req.method()));

    public static Decision.Fn isMethodGetOrHead = (r, req) ->
            CompletableFuture.completedFuture("GET".equalsIgnoreCase(req.method()) || "HEAD".equalsIgnoreCase(req.method()));

    public static Decision.Fn acceptExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("Accept").value().isPresent());

    public static Decision.Fn acceptableMediaTypeAvailable = (r, req) ->
            r.supportedMediaTypes(req).thenApply(mediaTypes -> bestMediaTypeFor(req, mediaTypes).isPresent());

    public static Decision.Fn isRedirect = (r, req) ->
            r.redirectAfterPost(req);

    public static Decision.Fn isNewResource = (r, req) ->
            r.createdNewResource(req);

    public static Decision.Fn resourcePreviouslyExisted = (r, req) ->
            r.resourcePreviouslyExisted(req);

    public static Decision.Fn resourceMovedPermanently = (r, req) ->
            r.resourceMovedPermanently(req);

    public static Completion.Fn movedPermanently = completionWithLocationAndStatus(301);

    public static Completion.Fn notFound = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(404)));

    public static Completion.Fn notAcceptable = completionWithDefaults((r, req) ->
            // TODO Unless it was a HEAD request, the response SHOULD include an entity containing a list of available entity characteristics
            CompletableFuture.completedFuture(new Response(406)));

    public static Completion.Fn seeOther = completionWithLocationAndStatus(303);

    public static Decision.Fn isPostForbidden = (r, req) ->
            r.isPostAllowed(req).thenApply(a -> !a);

    public static Decision.Fn isPostValid = (r, req) ->
            r.isPostValid(req);

    public static Decision.Fn isPutForbidden = (r, req) ->
            r.isPutAllowed(req).thenApply(a -> !a);

    public static Decision.Fn isPutValid = (r, req) ->
            r.isPutValid(req);

    public static Decision.Fn isDeleteForbidden = (r, req) ->
            r.isDeletetAllowed(req).thenApply(a -> !a);

    public static Decision.Fn serverPermitsPostToMissingResource = (r, req) ->
            r.isPostToMissingResourceAllowed(req);

    public static Decision.Fn isConflict = (r, req) ->
            r.isConflict(req);

    public static Completion.Fn conflict = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(409)));

    public static Completion.Fn accepted = completionWithDefaults((r, req) ->
            // TODO The entity returned with this response SHOULD include an indication of the request's current status and either a pointer to a status monitor or some estimate of when the user can expect the request to be fulfilled.
            CompletableFuture.completedFuture(new Response(202)));

    private static Function<Optional<String>, Boolean> optionalStringInHeader(Parsable<Optional<String>> header) {
        List<String> headerValues = header.parse(asList);
        return optionalString -> optionalString.filter(headerValues::contains).isPresent();
    }

    private static Function<Optional<Instant>, Boolean> optionalInstantAfterHeaderDate(Parsable<Optional<String>> header) {
        Instant headerDate = header.parse(asHttpDate).get();
        return optionalInstant -> optionalInstant
                .filter(instant -> instant.isAfter(headerDate))
                .isPresent();
    }

    public static Decision.Fn ifMatchExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Match").value().isPresent());

    public static Decision.Fn ifMatchStarExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Match").parse(asTrueIfEqualTo("*")));

    public static Decision.Fn etagInIfMatch = (r, req) ->
            r.etag(req).thenApply(optionalStringInHeader(req.header("If-Match")));

    public static Decision.Fn ifUnmodifiedSinceExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Unmodified-Since").value().isPresent());

    public static Decision.Fn ifUnmodifiedSinceIsValidDate = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Unmodified-Since").parse(asHttpDate).isPresent());

    public static Decision.Fn lastModifiedAfterIfUnmodifiedSince = (r, req) ->
            r.lastModified(req).thenApply(optionalInstantAfterHeaderDate(req.header("If-Unmodified-Since")));

    public static Decision.Fn ifNoneMatchExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-None-Match").value().isPresent());

    public static Decision.Fn ifNoneMatchStarExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-None-Match").parse(asTrueIfEqualTo("*")));

    public static Decision.Fn etagInIfNoneMatch = (r, req) ->
            r.etag(req).thenApply(optionalStringInHeader(req.header("If-None-Match")));

    public static Decision.Fn ifModifiedSinceExists = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Modified-Since").value().isPresent());

    public static Decision.Fn ifModifiedSinceIsValidDate = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-None-Match").parse(asHttpDate).isPresent());

    public static Decision.Fn ifModifiedSinceAfterNow = (r, req) ->
            CompletableFuture.completedFuture(req.header("If-Modified-Since")
                    .parse(asHttpDate)
                    .filter(ifModSince -> ifModSince.isAfter(Instant.now()))
                    .isPresent());

    public static Decision.Fn lastModifiedAfterIfModifiedSince = (r, req) ->
            r.lastModified(req).thenApply(optionalInstantAfterHeaderDate(req.header("If-Modified-Since")));

    public static Completion.Fn preconditionFailed = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(412)));

    public static Completion.Fn notModified = completionWithDefaults((r, req) ->
            CompletableFuture.completedFuture(new Response(304)));

    private static Node entityOrNoContent = decide("isRespondWithEntity", isRespondWithEntity)
            .onTrue(complete("ok", ok)) // TODO multiple representations decision
            .onFalse(complete("noContent", noContent));

    private static Node handlePost = decide("isPostForbidden", isPostForbidden)
            .onTrue(complete("forbidden", forbidden))
            .onFalse(decide("isPostValid", isPostValid)
                    .onTrue(act("doPost", doPost).andThen(decide("isRedirect", isRedirect)
                            .onTrue(complete("seeOther", seeOther))
                            .onFalse(decide("isNewResource", isNewResource)
                                    .onTrue(complete("created", created))
                                    .onFalse(entityOrNoContent))))
                    .onFalse(complete("badRequest", badRequest)));

    private static Node handlePut = decide("isPutForbidden", isPutForbidden)
            .onTrue(complete("forbidden", forbidden))
            .onFalse(decide("isConflict", isConflict)
                    .onTrue(complete("conflict", conflict))
                    .onFalse(decide("isPutValid", isPutValid)
                            .onTrue(act("doPut", doPut).andThen(decide("isNewResource", isNewResource)
                                    .onTrue(complete("created", created))
                                    .onFalse(entityOrNoContent)))
                            .onFalse(complete("badRequest", badRequest))));

    private static Node handleDelete = decide("isDeleteForbidden", isDeleteForbidden)
            .onTrue(complete("forbidden", forbidden))
            .onFalse(act("doDelete", doDelete)
                    .andThen(decide("isDeleteEnacted", isDeleteEnacted)
                            .onTrue(entityOrNoContent)
                            .onFalse(complete(accepted))));

    private static Node resourceDoesntExist = decide("isMethodPut", isMethodPut)
            .onTrue(complete(notYetImplemented))
            .onFalse(decide("resourcePreviouslyExisted", resourcePreviouslyExisted)
                    .onTrue(decide("resourceMovedPermanently", resourceMovedPermanently)
                            .onTrue(complete("movedPermanently", movedPermanently))
                            .onFalse(complete(notYetImplemented)))
                    .onFalse(decide("isMethodPost", isMethodPost)
                            .onTrue(decide("serverPermitsPostToMissingResource", serverPermitsPostToMissingResource)
                                    .onTrue(handlePost)
                                    .onFalse(complete("notFound", notFound)))
                            .onFalse(complete("notFound", notFound))));

    private static Node afterConditionalHandling = decide("isMethodDelete", isMethodDelete)
            .onTrue(handleDelete)
            .onFalse(decide("isMethodPost", isMethodPost)
                    .onTrue(handlePost)
                    .onFalse(decide("isMethodPut", isMethodPut)
                            .onTrue(handlePut)
                            .onFalse(entityOrNoContent)));

    private static Node decideIfModifiedSinceExists = decide("ifModifiedSinceExists", ifModifiedSinceExists)
            .onTrue(decide("ifModifiedSinceIsValidDate", ifModifiedSinceIsValidDate)
                    .onTrue(decide("ifModifiedSinceAfterNow", ifModifiedSinceAfterNow)
                            .onTrue(afterConditionalHandling)
                            .onFalse(decide("lastModifiedAfterIfModifiedSince", lastModifiedAfterIfModifiedSince)
                                    .onTrue(afterConditionalHandling)
                                    .onFalse(complete("notModified", notModified))))
                    .onFalse(afterConditionalHandling))
            .onFalse(afterConditionalHandling);

    private static Node decideIfMethodGetOrHead = decide("isMethodGetOrHead", isMethodGetOrHead)
            .onTrue(complete("notModified", notModified))
            .onFalse(complete("preconditionFailed", preconditionFailed));

    private static Node decideIfNoneMatchExists = decide("ifNoneMatchExists", ifNoneMatchExists)
            .onTrue(decide("ifNoneMatchStarExists", ifNoneMatchStarExists)
                    .onTrue(decideIfMethodGetOrHead)
                    .onFalse(decide("etagInIfNoneMatch", etagInIfNoneMatch)
                            .onTrue(decideIfMethodGetOrHead)
                            .onFalse(decideIfModifiedSinceExists)))
            .onFalse(decideIfModifiedSinceExists);

    private static Node decideIfUnmodifiedSinceExists = decide("ifUnmodifiedSinceExists", ifUnmodifiedSinceExists)
            .onTrue(decide("ifUnmodifiedSinceIsValidDate", ifUnmodifiedSinceIsValidDate)
                    .onTrue(decide("lastModifiedAfterIfUnmodifiedSince", lastModifiedAfterIfUnmodifiedSince)
                            .onTrue(complete("preconditionFailed", preconditionFailed))
                            .onFalse(decideIfNoneMatchExists))
                    .onFalse(decideIfNoneMatchExists))
            .onFalse(decideIfNoneMatchExists);

    private static Node resourceDoesExist = decide("ifMatchExists", ifMatchExists)
            .onTrue(decide("ifMatchStarExists", ifMatchStarExists)
                    .onTrue(decideIfUnmodifiedSinceExists)
                    .onFalse(decide("etagInIfMatch", etagInIfMatch)
                            .onTrue(decideIfUnmodifiedSinceExists)
                            .onFalse(complete("preconditionFailed", preconditionFailed))))
            .onFalse(decideIfUnmodifiedSinceExists);

    private static Node existingResourceDecision = decide("isExistingResource", isExistingResource)
            .onTrue(resourceDoesExist)
            .onFalse(resourceDoesntExist);

    private static Node contentNegotiation = decide("acceptExists", acceptExists)
            .onTrue(decide("acceptableMediaTypeAvailable", acceptableMediaTypeAvailable)
                    .onTrue(existingResourceDecision)
                    .onFalse(complete("notAcceptable", notAcceptable)))
            .onFalse(existingResourceDecision);

    private static Node decisionFlow = decide("isServiceAvailable", isServiceAvailable)
            .onTrue(decide("isKnownMethod", isKnownMethod)
                    .onTrue(decide("isMethodAllowed", isMethodAllowed)
                            .onTrue(decide("isMalformed", isMalformed)
                                    .onTrue(complete("badRequest", badRequest))
                                    .onFalse(decide("isUnauthorized", isUnauthorized)
                                            .onTrue(complete("unauthorized", unauthorized))
                                            .onFalse(decide("isForbidden", isForbidden)
                                                    .onTrue(complete("forbidden", forbidden))
                                                    .onFalse(decide("unknownContentType", unknownContentType)
                                                            .onTrue(complete("unsupportedMediaType", unsupportedMediaType))
                                                            .onFalse(decide("isMethodOptions", isMethodOptions)
                                                                    .onTrue(complete("options", options))
                                                                    .onFalse(contentNegotiation))))))
                            .onFalse(complete("methodNotAllowed", methodNotAllowed)))
                    .onFalse(complete("unknownMethod", unknownMethod)))
            .onFalse(complete("serviceNotAvailable", serviceNotAvailable));

    public static Node get() {
        return decisionFlow;
    }
}
