package webster.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webster.requestresponse.Request;
import webster.requestresponse.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);

    private final ExecutorService executorService;
    private final int port;
    private final long timeoutMillis;

    public Server(ExecutorService executorService, int port, long timeoutMillis) {
        this.executorService = executorService;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }

    public void run(Function<Request, CompletableFuture<Response>> requestHandler) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            int maxContentLength = 64 * 1024;
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(maxContentLength));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new HttpHandler(requestHandler, executorService, timeoutMillis));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            logger.info("listening on port " + port);

            // wait until server socket is closed
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static class Builder {
        private ExecutorService executorService = ForkJoinPool.commonPool();
        private int port = 8080;
        private long timeoutMillis = 30000l;

        public Server build() {
            return new Server(executorService, port, timeoutMillis);
        }

        public Builder withExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withTimeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }
    }
}
