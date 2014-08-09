package org.wetty.httpserver.server;

import org.wetty.httpserver.utils.statistics.ChannelGatherable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

public class HttpWettyServerTrafficHandler extends ChannelTrafficShapingHandler implements ChannelGatherable {

	private StringBuilder url = new StringBuilder();
	
	public StringBuilder getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = new StringBuilder();
 		this.url.append(url);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelUnregistered(ctx);
		//TODO: Gather accumulated statistics to optimize write speed, number of requests
		gatherStatistics(ctx.channel());
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {

		super.write(ctx, msg, promise);	
		//gatherStatistics(ctx.channel());

	}

	@Override
	protected void doAccounting(TrafficCounter counter) {
		super.doAccounting(counter); //NOOP
		//TODO: count avg speed
	}

	public HttpWettyServerTrafficHandler(long checkInterval) {
		super(checkInterval);
	}

	@Override
	public void read(ChannelHandlerContext ctx) {
		super.read(ctx);
		//gatherStatistics(ctx.channel());
	}

	@Override
	public TrafficCounter trafficCounter() {
		return super.trafficCounter();	
	}

	//TODO: move to other class
	public String printTrafficCounter(TrafficCounter counter) {
		return "Monitor " + counter.name() + " Current Speed Read: " +
				(counter.lastReadThroughput()) + " bytes/s, Write: " +
				(counter.lastWriteThroughput()) + " bytes/s Current Read: " +
				(counter.currentReadBytes()) + " bytes Current Write: " +
				(counter.currentWrittenBytes()) + " bytes";
	}

	@Override
	public void gatherStatistics(Channel channel) {
		TrafficCounter counter = trafficCounter();
		
		if (channel.parent() instanceof HttpWettyServerChannel) {
			((HttpWettyServerChannel) channel.parent()).getStatistics().gatherFromTrafficCounter(channel, counter, this.url.toString());
		}		
		
		trafficCounter().resetCumulativeTime();
	}


}
