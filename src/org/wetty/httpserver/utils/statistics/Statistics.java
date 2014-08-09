package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public interface Statistics {
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter, String url);
	public void gatherFromChannel(Channel channel);
	public void gatherRedirect(String url);
}
