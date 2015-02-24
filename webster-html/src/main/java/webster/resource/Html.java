package webster.resource;

import org.fusesource.scalate.japi.TemplateEngineFacade;
import webster.Scalate;
import webster.requestresponse.Request;
import webster.requestresponse.ResponseBody;
import webster.requestresponse.Responses;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Html {

    static final TemplateEngineFacade engine = Scalate.layoutTemplateEngine();

    default Map<String, Object> modelWithDefaults(Map<String, Object> model, Request request) {
        model.put("_request", request);
        model.put("_flash", request.flash());
        return model;
    }

    default String renderHtml(String template, Map<String, Object> model, Request request) {
        return engine.layout(template, modelWithDefaults(model, request));
    }

    default Function<Request, CompletableFuture<ResponseBody>> htmlProducer(
            String template, Function<Request, CompletableFuture<Map<String, Object>>> modelFactory) {
        return req -> modelFactory.apply(req).thenApply(model -> Responses.bodyFrom(renderHtml(template, model, req)));
    }
}
