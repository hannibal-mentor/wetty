/*
 * Based on Netty examples
 * All settings should be set here
 */
package org.wetty.httpserver.server;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.HibernateUtil;
import org.wetty.httpserver.utils.statistics.SimpleStatistics;
import org.wetty.httpserver.views.HTMLViewBuilder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import static org.wetty.httpserver.utils.Constants.Names.*;

public class HttpWettyServer {

	private static final long CHECKINTERVAL = Integer.parseInt(System.getProperty("check_interval", "1000"));

	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "9443" : "9090"));

	public static void main(String[] args) throws Exception {

		//Initialize Hibernate connection pool
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.close();

		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
		} else {
			sslCtx = null;
		}

		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup(10);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childAttr(STATISTICS_CLASS, SimpleStatistics.class)
			.childAttr(CONTROLLERMANAGER_CLASS , ControllerManager.class)
			.childAttr(VIEWBUILDER_CLASS , HTMLViewBuilder.class)
			.childHandler(new HttpWettyServerInitializer(sslCtx, CHECKINTERVAL));

			Channel ch = b.bind(PORT).sync().channel();

			System.err.println("Open your web browser and navigate to " +
					(SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

			ch.closeFuture().sync();
		} finally {
			sessionFactory.close();

			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}