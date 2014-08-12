/*
 * Class to handle controllers
 * Should find view
 */

package org.wetty.httpserver.controllers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.wetty.httpserver.views.ViewBuilder;

public class ControllerManager {

	//VIEW
	ViewBuilder viewBuilder;

	//MODEL:
	//Object msg
	//ChannelHandlerContext ctx
	private HttpRequest request;

	private final StringBuilder buf = new StringBuilder();

	//empty constructor initialization is not allowed, we need to pass request to view correctly
	private ControllerManager() {};

	public ControllerManager(ViewBuilder viewBuilder) {
		this();
		this.viewBuilder = viewBuilder;
	}

	public void getView(ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof HttpRequest) {

			HttpRequest request = this.request = (HttpRequest) msg;
			viewBuilder.setRequest(request);

			//clear buffer
			buf.setLength(0);	

			//viewBuilder is responsible for parsing HTTP-request (msg), getting view and building HTTP-response
			buf.append(getViewByRequest(ctx, msg));
			viewBuilder.appendDecoderResult(buf, request);
		}

		if (msg instanceof HttpContent) {
			viewBuilder.appendEndOfContent(buf, ctx, msg);
		}
	}

	//let the view builder build the output
	public Object getViewByRequest(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {

			String url = request.getUri();
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
			String path = queryStringDecoder.path();

			switch (path) {
			case "/hello": 
				return viewBuilder.processHello(ctx, msg);
			case "/redirect": 
				return viewBuilder.processRedirect(ctx, msg);
			case "/status": 
				return viewBuilder.processStatus(ctx, msg);
			default:
				return viewBuilder.def(msg);
			}
		}	
		else {
			//if not HttpRequest
			return viewBuilder.def(msg);
		}
	}

}
