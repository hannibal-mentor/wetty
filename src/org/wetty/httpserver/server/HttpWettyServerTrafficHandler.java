package org.wetty.httpserver.server;

import org.wetty.httpserver.utils.AttributeClassSpawner;
import org.wetty.httpserver.utils.statistics.ChannelGatherable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

public class HttpWettyServerTrafficHandler extends ChannelTrafficShapingHandler implements ChannelGatherable {

	private final StringBuilder url = new StringBuilder();
	//traffic handler is in the same thread as a channel, so we do not need volatile modifier
	
	public StringBuilder getUrl() {
		return url;
	}

	public void setUrlAndGather(String url, Channel channel) {
		if (this.url.length() > 0 && !this.url.equals(url)) {
			//reset and gather
			gatherStatistics(channel);
		}
		
		this.url.setLength(0);
 		this.url.append(url);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		//Gathering accumulated statistics to optimize write speed, number of requests
		gatherStatistics(ctx.channel());
	}

	@Override
	protected void doAccounting(TrafficCounter counter) {
		super.doAccounting(counter); //NOOP
	}

	public HttpWettyServerTrafficHandler(long checkInterval) {
		super(checkInterval);
	}

	@Override
	public void gatherStatistics(Channel channel) {
		AttributeClassSpawner.createStatisticsClass(channel).gatherFromTrafficCounter(channel, trafficCounter(), this.url.toString());
	}

}
