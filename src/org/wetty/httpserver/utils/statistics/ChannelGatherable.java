package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;

public interface ChannelGatherable {
	public void gatherStatistics(Channel channel);
}
