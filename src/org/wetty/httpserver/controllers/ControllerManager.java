/*
 * Class to handle controllers
 * Should find view
 */

package org.wetty.httpserver.controllers;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import org.wetty.httpserver.server.HttpWettyServerChannel;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

public class ControllerManager {
	
	//TODO:
	//private Object model;
	//private Object view;
	
	public Object getView(ViewBuilder v, ChannelHandlerContext ctx, Object msg) {
		
		if (msg instanceof HttpRequest) {
		
			HttpRequest request = ((HttpRequest) msg);
			
			String url = request.getUri();
			
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
		        
			String path = queryStringDecoder.path();
			StringBuilder redirect = new StringBuilder(); 
			 	Map<String, List<String>> params = queryStringDecoder.parameters();
		        if (!params.isEmpty()) {
		            for (Entry<String, List<String>> p: params.entrySet()) {
		                String key = p.getKey();
		                List<String> vals = p.getValue();
		                for (String val : vals) {
		                	if (key.equals("url")) {
		                		redirect.append(val);
		                	}
		                }
		            }
		        }
			
				//TODO: generalize
			switch (path) {
				case "/hello": 
					if (is100ContinueExpected(request)) {
		                send100Continue(ctx);
		            }
					return v.processHello(msg);
				case "/redirect": 
					send302(ctx, redirect.toString());
					return v.processRedirect(msg);
				case "/status": 
					if (is100ContinueExpected(request)) {
		                send100Continue(ctx);
		            }
					return v.processStatus(msg);
			default:
					send404(ctx);
		            return v.error404(msg);
			}
			
		}	
		else {
			//TODO: if not HttpRequest
			return v.def(msg);
		}
	}

	 private void send404(ChannelHandlerContext ctx) {
		 FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
         ctx.write(response);
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
         FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
         ctx.write(response);
     }
	 
	 private static void send302(ChannelHandlerContext ctx, String redirect) {
		 Channel channel = ctx.channel(); 

		 if (channel.parent() instanceof HttpWettyServerChannel) {    		
     		Statistics statistics = ((HttpWettyServerChannel) ctx.channel().parent()).getStatistics();
     		statistics.gatherRedirect(redirect);
     	 }
		 
         FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
         response.headers().set(LOCATION, redirect);
         ctx.write(response);
     }
	
	public void setResponse(HttpRequest request, ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		
	}
}
