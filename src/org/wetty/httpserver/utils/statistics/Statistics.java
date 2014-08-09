package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public interface Statistics {
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter);
	public void gatherFromChannel(Channel channel);
}
