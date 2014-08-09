/*
 * Based on Netty examples
 */
package org.wetty.httpserver.server;

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.statistics.SimpleStatistics;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class HttpWettyServer {

	//TODO: add counter object here?

	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "9443" : "9090"));

	public static void main(String[] args) throws Exception {
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		} else {
			sslCtx = null;
		}

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			//.channel(NioServerSocketChannel.class)
			.channel(HttpWettyServerChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new HttpWettyServerInitializer(sslCtx));

			Channel ch = b.bind(PORT).sync().channel();

			if (ch instanceof HttpWettyServerChannel) {
				((HttpWettyServerChannel) ch).setStatistics((Statistics) new SimpleStatistics());
				((HttpWettyServerChannel) ch).setRequestHandler((RequestHandler) new HttpWettyServerRequestHandler());
				((HttpWettyServerChannel) ch).setControllerManager(new ControllerManager());
				((HttpWettyServerChannel) ch).setViewBuilder(new ViewBuilder());
			}
			
			System.err.println("Open your web browser and navigate to " +
					(SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

			//TODO: Bind model, view, controller, statistics and request-response handler implementations
			//TODO: initialize statistics handler
			//TODO: initialize view handler
			//SimpleStatistics, HttpWettyServerRequestHandler

			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}