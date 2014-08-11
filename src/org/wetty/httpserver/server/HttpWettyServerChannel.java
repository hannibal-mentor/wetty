package org.wetty.httpserver.server;

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpWettyServerChannel extends NioServerSocketChannel{

	private Statistics statistics;
	private ControllerManager controllerManager;
	
	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public void setControllerManager(ControllerManager controllerManager) {
		this.controllerManager = controllerManager;
	}

	public HttpWettyServerChannel() {
		super();
	}
	
	public HttpWettyServerChannel(Statistics statistics, ControllerManager controllerManager, ViewBuilder viewBuilder) {
		super();
		
		this.statistics = statistics;
		this.controllerManager = controllerManager;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	public ControllerManager getControllerManager() {
		return controllerManager;
	}

	public static HttpWettyServerChannel getParentServerChannel(Channel channel) {
		if (channel.parent() instanceof HttpWettyServerChannel) {
			return ((HttpWettyServerChannel) channel.parent());
		}

		else return null;
	}

}
