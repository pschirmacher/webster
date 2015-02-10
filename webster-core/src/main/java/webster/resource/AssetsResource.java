package webster.resource;

import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;
import webster.util.Maps;

import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AssetsResource implements Resource {

    private static final Instant serverStart = Instant.now();

    private final String root;
    private final Map<String, String> fileExtensionToMimeType;

    public AssetsResource() {
        this("/public");
    }

    public AssetsResource(String root) {
        this(root, Maps.newStringMap().with("js", "application/javascript").build());
    }

    public AssetsResource(String root, Map<String, String> fileExtensionToMimeType) {
        this.fileExtensionToMimeType = fileExtensionToMimeType;
        this.root = withTrailingSlash(root);
    }

    private String withTrailingSlash(String s) {
        return s.endsWith("/") ? s : s + "/";
    }

    private String resourcePath(Request request) {
        return request.splats().value().get(0);
    }

    @Override
    public CompletableFuture<Boolean> doesRequestedResourceExist(Request request) {
        InputStream resourceAsStream = Object.class.getResourceAsStream(root + resourcePath(request));
        request.context().put("asset", resourceAsStream);
        return completedFuture(resourceAsStream != null);
    }

    @Override
    public CompletableFuture<ResponseBody> entity(Request request) {
        return completedFuture(request.context().getExisting("asset"));
    }

    @Override
    public CompletableFuture<Optional<Instant>> lastModified(Request request) {
        return completedFuture(request.context().get("asset").map(a -> serverStart));
    }

    @Override
    public CompletableFuture<Optional<String>> etag(Request request) {
        return lastModified(request).thenApply(lastModified -> lastModified.map(instant ->
                (resourcePath(request) + instant.toEpochMilli()).replace(",", "")));
    }

    @Override
    public CompletableFuture<Set<String>> supportedMediaTypes(Request request) {
        String assetName = lastElement(resourcePath(request).split("/"));
        String mediaType = assetName.contains(".")
                ? typeFor(lastElement(assetName.split("\\.")))
                : "application/octet-stream";
        return completedFuture(Collections.singleton(mediaType));
    }

    @Override
    public CompletableFuture<Map<String, String>> additionalHeaders(int responseStatus, Request request) {
        // TODO make max-age configurable
        // TODO only set on status 200?
        return completedFuture(responseStatus == 200
                ? Maps.newStringMap().with("Cache-Control", "max-age=3600, must-revalidate").build()
                : Collections.emptyMap());
    }

    private <T> T lastElement(T[] array) {
        return array[array.length - 1];
    }

    private String typeFor(String fileExtension) {
        String type = fileExtensionToMimeType.get(fileExtension);
        return type != null ? type : "application/" + fileExtension;
    }
}
