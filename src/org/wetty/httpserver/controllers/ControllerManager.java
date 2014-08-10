/*
 * Class to handle controllers
 * Should find view
 */

package org.wetty.httpserver.controllers;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import org.wetty.httpserver.server.HttpWettyServerHandler;
import org.wetty.httpserver.views.ViewBuilder;

public class ControllerManager {
	
	//TODO:
	//private Object model;
	//private Object view;
	
	//Finds controller and sends request
	public Object getView(ViewBuilder v, ChannelHandlerContext ctx, Object msg) {
		
		if (msg instanceof HttpRequest) {
		
			HttpRequest request = ((HttpRequest) msg);
			
			String url = request.getUri();
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
		    String path = queryStringDecoder.path();
			
			//TODO: generalize
			switch (path) {
				case "/hello": 
					if (is100ContinueExpected(request)) {
						HttpWettyServerHandler.send100(ctx);
		            }
					return v.processHello(msg);
				case "/redirect": 
					
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
					
					HttpWettyServerHandler.send302(ctx, redirect.toString());
					return v.processRedirect(msg);
				case "/status": 
					if (is100ContinueExpected(request)) {
						HttpWettyServerHandler.send100(ctx);
		            }
					return v.processStatus(msg);
			default:
					HttpWettyServerHandler.send404(ctx);
		            return v.error404(msg);
			}
			
		}	
		else {
			//TODO: if not HttpRequest
			return v.def(msg);
		}
	}


	public void setResponse(HttpRequest request, ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		
	}
}
