    /*
     * Counts active connections by holding channels info
     */

package org.wetty.httpserver.utils.statistics;

import io.netty.channel.Channel;

import java.util.Hashtable;

public class ChannelHolder {
	private static volatile Hashtable<Integer,Channel> channels = new Hashtable<Integer,Channel>();
	
	public static synchronized void add(Channel channel) {
		if (!channels.containsKey(channel.hashCode())) {
			channels.put(channel.hashCode(), channel);
		};
	}
	
	public static synchronized void remove(Channel channel) {
		//TODO: check if channel is active
		
		channels.remove(channel.hashCode());
	}
	
	public static synchronized int size() {
		return channels.values().size();
	}
}
