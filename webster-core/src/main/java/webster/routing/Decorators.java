package webster.routing;

import webster.requestresponse.Request;
import webster.requestresponse.Response;
import webster.requestresponse.parsing.Parsers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static webster.requestresponse.parsing.Parsers.asTrueIfEqualTo;

public class Decorators {

    public static final UnaryOperator<Function<Request, CompletableFuture<Response>>> parseHiddenMethodFromForms =
            handler -> req -> {
                // TODO multipart/form-data?
                boolean formEncoded = req.header("Content-Type").parse(asTrueIfEqualTo("application/x-www-form-urlencoded"));
                if (formEncoded && "POST".equals(req.method())) {
                    String actualMethod = req.body().parse(Parsers.asForm).getOrDefault("_method", req.method().toUpperCase());
                    return handler.apply(req.withMethod(actualMethod));
                } else {
                    return handler.apply(req);
                }
            };

    public static final UnaryOperator<Function<Request, CompletableFuture<Response>>> logRequestResponse =
            handler -> req -> {
                // TODO logging
                System.out.println(req);
                return handler.apply(req)
                        .whenComplete((resp, throwable) -> {
                            if (resp != null)
                                System.out.println(resp);
                            else if (throwable != null)
                                throwable.printStackTrace();
                        });
            };
}
