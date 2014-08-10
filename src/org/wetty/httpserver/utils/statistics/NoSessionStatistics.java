package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class NoSessionStatistics extends SimpleStatistics {

	@Override
	public void gatherFromTrafficCounter(Channel channel,
			TrafficCounter trafficCounter, String url) {
		// Nope
		
	}
}
