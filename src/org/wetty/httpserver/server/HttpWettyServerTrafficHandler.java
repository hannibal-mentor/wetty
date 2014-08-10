package org.wetty.httpserver.server;

import org.wetty.httpserver.utils.statistics.ChannelGatherable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

public class HttpWettyServerTrafficHandler extends ChannelTrafficShapingHandler implements ChannelGatherable {

	private volatile StringBuilder url = new StringBuilder();
	
	public StringBuilder getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = new StringBuilder();
 		this.url.append(url);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		//Gathering accumulated statistics to optimize write speed, number of requests
		gatherStatistics(ctx.channel());
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {

		super.write(ctx, msg, promise);	
		//System.out.println("WRITE: "+ ctx.channel() + tcInfo());
	}

	@Override
	protected void doAccounting(TrafficCounter counter) {
		super.doAccounting(counter); //NOOP

	}

	public HttpWettyServerTrafficHandler(long checkInterval) {
		super(checkInterval);
	}

	@Override
	public void read(ChannelHandlerContext ctx) {
		super.read(ctx);
		//System.out.println("READ: "+ ctx.channel() + tcInfo());
	}

	@Override
	public TrafficCounter trafficCounter() {
		return super.trafficCounter();	
	}

	@Override
	public void gatherStatistics(Channel channel) {
		TrafficCounter counter = trafficCounter();
		
		synchronized (counter) {
			if (channel.parent() instanceof HttpWettyServerChannel) {
				((HttpWettyServerChannel) channel.parent()).getStatistics().gatherFromTrafficCounter(channel, counter, this.url.toString());
			}		
		}
		
	}


}
