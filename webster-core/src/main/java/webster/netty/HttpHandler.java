package webster.netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webster.requestresponse.*;
import webster.util.Futures;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    private final Function<Request, CompletableFuture<Response>> requestHandler;
    private final ExecutorService executor;
    private final long timeoutMillis;

    public HttpHandler(Function<Request, CompletableFuture<Response>> requestHandler,
                       ExecutorService executor,
                       long timeoutMillis) {
        super(false);
        this.requestHandler = requestHandler;
        this.executor = executor;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        if (is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
        boolean keepAlive = isKeepAlive(req);
        Request request = createRequest(req);

        // requestHandler is a function that takes a request and creates a future. The creation of the future
        // might block. That's why requestHandler#apply is run on another thread.
        CompletableFuture<Response> timeout = timeoutResponseFuture();
        CompletableFuture
                .supplyAsync(() -> requestHandler.apply(request), executor) // creation of the future
                .thenCompose(f -> f.exceptionally(Response::new)) // handle exceptions during future creation
                .acceptEither(timeout, r -> {
                    ReferenceCountUtil.release(req);
                    handleResponse(r, ctx, keepAlive); // handle response of either timeout or requestHandler
                })
                .whenComplete((v, e) -> {
                    if(e != null)
                        exceptionCaught(ctx, e);
                });
    }

    private CompletableFuture<Response> timeoutResponseFuture() {
        return Futures.afterTimeout(new Response(500, Responses.bodyFrom("request processing timed out")), timeoutMillis);
    }

    private void handleResponse(Response response, ChannelHandlerContext context, boolean keepAlive) {
        response.body().process(new ResponseBodyProcessor<Void>() {
            @Override
            public Void process(StringResponseBody body) {
                handleFullResponse(
                        createFullResponse(response.status(), response.headers(), body.content()),
                        context, keepAlive);
                return null;
            }

            @Override
            public Void process(InputStreamResponseBody body) {
                handleStreamResponse(response.status(), response.headers(), body.content(), context, keepAlive);
                return null;
            }

            @Override
            public Void process(EmptyResponseBody body) {
                handleFullResponse(
                        createFullResponse(response.status(), response.headers(), ""),
                        context, keepAlive);
                return null;
            }
        });
    }

    private void handleFullResponse(FullHttpResponse response, ChannelHandlerContext context, boolean keepAlive) {
        if (!keepAlive) {
            context.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            context.write(response);
        }
        context.flush();
    }

    private void handleStreamResponse(int status, Map<String, String> headers, InputStream body,
                                      ChannelHandlerContext context, boolean keepAlive) {
        // TODO no chunked encoding for http 1.0 clients
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status));
        response.headers().set(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        headers.entrySet().stream().forEach(header ->
                response.headers().set(header.getKey(), header.getValue()));
        context.write(response);

        context.write(new ChunkedStream(body));
        ChannelFuture lastContentFuture = context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private FullHttpResponse createFullResponse(int status, Map<String, String> headers, String body) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1,
                HttpResponseStatus.valueOf(status),
                body.isEmpty() ? Unpooled.buffer(0) : Unpooled.wrappedBuffer(body.getBytes(Charset.forName("UTF-8")))); // TODO charset
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        headers.entrySet().stream().forEach(header ->
                response.headers().set(header.getKey(), header.getValue()));
        return response;
    }

    private Request createRequest(FullHttpRequest req) {
        InputStream body = new ByteBufInputStream(req.content());
        Map<String, String> headers = new HashMap<>(req.headers().names().size());
        for (String header : req.headers().names()) {
            String headerValue = req.headers().getAll(header).stream().collect(Collectors.joining(","));
            headers.put(header, headerValue);
        }
        QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());// TODO charset
        return new Request(req.getMethod().name(), decoder.path(), Collections.unmodifiableMap(headers), body,
                decoder.parameters(), null, null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(500));
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
