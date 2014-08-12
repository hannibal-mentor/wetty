/*
 * Based on Netty examples
 */    

package org.wetty.httpserver.server;

import org.wetty.httpserver.controllers.ControllerManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.wetty.httpserver.utils.AttributeClassSpawner;
import org.wetty.httpserver.utils.statistics.ChannelGatherable;
import org.wetty.httpserver.utils.statistics.Statistics;

public class HttpWettyServerHandler extends SimpleChannelInboundHandler<Object> implements ChannelGatherable {

	private Statistics statistics = null;
	private ControllerManager controllerManager = null;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		gatherStatistics(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		gatherStatistics(ctx.channel());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx)
			throws Exception {
		super.channelRegistered(ctx);
		gatherStatistics(ctx.channel());
	}

	@Override
	public void gatherStatistics(Channel channel) {
		if (statistics == null) {
			statistics = AttributeClassSpawner.createStatisticsClass(channel);
		}

		if (statistics != null) {
			statistics.gatherFromChannel(channel);
		}
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx)
			throws Exception {
		super.channelUnregistered(ctx);
		gatherStatistics(ctx.channel());		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		//cause.printStackTrace(); //We will not print this to stack
		ctx.close();
		gatherStatistics(ctx.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
		Channel channel = ctx.channel();
		
		//Initializing statistics request parameters parameters on channel read and handling the request	
		if (msg instanceof HttpRequest) {
			if (statistics == null) {
				statistics = AttributeClassSpawner.createStatisticsClass(channel);
			}
			if (statistics != null) {
				statistics.setChannelParameters(channel, msg);
			}
		}		
		gatherStatistics(channel);

		if (controllerManager == null) {
			controllerManager = AttributeClassSpawner.createControllerManagerClass(channel);
		};

		if (msg instanceof HttpContent || msg instanceof HttpRequest) {
			if (controllerManager != null) {
				controllerManager.getView(ctx, msg);
			}
		}

	}

}