package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class NoStatistics implements Statistics {

	@Override
	public void gatherFromTrafficCounter(Channel channel,
			TrafficCounter trafficCounter, String url) {
		// Nope
		
	}

	@Override
	public void gatherFromChannel(Channel channel) {
		// Nope
		
	}

	@Override
	public void gatherRedirect(String url) {
		// Nope
		
	}

}
