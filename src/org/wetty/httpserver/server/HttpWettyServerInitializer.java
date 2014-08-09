/*
 * Based on Netty examples
 */
package org.wetty.httpserver.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

public class HttpWettyServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	private final long checkInterval;

	public HttpWettyServerInitializer(SslContext sslCtx, long checkInterval) {
		this.sslCtx = sslCtx;
		this.checkInterval = checkInterval;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		
		p.addLast(new HttpWettyServerTrafficHandler(checkInterval));
		
		p.addLast(new HttpRequestDecoder());
		
		p.addLast(new HttpResponseEncoder());

		//automatic content compression.
		p.addLast(new HttpContentCompressor());

		p.addLast(new HttpWettyServerHandler());
		
	}
}