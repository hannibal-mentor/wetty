
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	public void write() {
		
	}

	@Override
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter) {
		System.out.println(channel.remoteAddress().toString() + ", written: "+ trafficCounter.cumulativeWrittenBytes());
		
	}

	@Override
	public void gatherFromChannel(Channel channel) {
		//TODO: Store remote port
        //System.out.println(ctx.channel().remoteAddress());
        //String host = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
        //int port = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getPort();
		
		if (channel.isActive()) {
			System.out.println("Channel is active "+channel.toString());
			ChannelHolder.add(channel);
		} else {
			System.out.println("Channel is not active "+channel.toString());
			ChannelHolder.remove(channel);
		}
	}
}
