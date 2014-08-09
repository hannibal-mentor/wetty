package org.wetty.httpserver.server;

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpWettyServerChannel extends NioServerSocketChannel{

	private Statistics statistics;
	private RequestHandler requestHandler;
	private ControllerManager controllerManager;
	private ViewBuilder viewBuilder;
	
	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public void setRequestHandler(RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
	}

	public void setControllerManager(ControllerManager controllerManager) {
		this.controllerManager = controllerManager;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public HttpWettyServerChannel() {
		super();
	}
	
	public HttpWettyServerChannel(Statistics statistics, RequestHandler requestHandler, ControllerManager controllerManager, ViewBuilder viewBuilder) {
		super();
		
		this.statistics = statistics;
		this.requestHandler = requestHandler;
		this.controllerManager = controllerManager;
		this.viewBuilder = viewBuilder;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	public RequestHandler getRequestHandler() {
		return requestHandler;
	}

	public ControllerManager getControllerManager() {
		return controllerManager;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	//TODO: remove
	public void me() {
		System.out.println("It's me");
	}
}
