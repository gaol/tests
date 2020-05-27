package io.github.gaol.tests.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpServer {
    public static void main(String[] args) throws Exception {
        System.out.println("Trying to start a HTTP Server on port: 8080...");

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());
            Channel ch = b.bind(8080).sync().channel();

            System.out.println("Http Server started, try to run ./run-test.sh to reproduce the issue.");

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpRequestDecoder());
            p.addLast(new HttpResponseEncoder());
            p.addLast(new HttpServerHandler());
        }
    }

    private static class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            System.out.println("Read Message: " + msg);
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                if (req.decoderResult().isFailure()) {
                    System.out.println("Error on decoding");
                    req.decoderResult().cause().printStackTrace();
                    ctx.close();
                    return;
                }

                boolean keepAlive = HttpUtil.isKeepAlive(req);
                FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), OK,
                        Unpooled.copiedBuffer("I hope you can get my response!", CharsetUtil.UTF_8));
                response.headers()
                        .set(CONTENT_TYPE, TEXT_PLAIN)
                        .setInt(CONTENT_LENGTH, response.content().readableBytes());

                if (keepAlive) {
                    if (!req.protocolVersion().isKeepAliveDefault()) {
                        response.headers().set(CONNECTION, KEEP_ALIVE);
                    }
                } else {
                    // Tell the client we're going to close the connection.
                    response.headers().set(CONNECTION, CLOSE);
                }

                ChannelFuture f = ctx.write(response);

                if (!keepAlive) {
                    f.addListener(ChannelFutureListener.CLOSE);
                }
            }
//            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
//                    HttpResponseStatus.OK,
//                    Unpooled.copiedBuffer("I hope you can get my response!", CharsetUtil.UTF_8));
//            ctx.write(response);
//            ctx.close();
        }

    }
}
