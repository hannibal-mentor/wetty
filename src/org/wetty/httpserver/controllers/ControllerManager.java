/*
 * Class to handle controllers
 * Should find view
 */

package org.wetty.httpserver.controllers;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import org.wetty.httpserver.server.HttpWettyServerChannel;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

public class ControllerManager {

	ViewBuilder viewBuilder;

	//MODEL:
	//Object msg
	//ChannelHandlerContext ctx

	private final StringBuilder buf = new StringBuilder();

	private HttpRequest request;

	public ControllerManager(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	//Finds controller and sends request
	public Object getView(ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof HttpRequest) {

			HttpRequest request = ((HttpRequest) msg);

			String url = request.getUri();
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
			String path = queryStringDecoder.path();

			//TODO: generalize
			switch (path) {
			case "/hello": 
				if (is100ContinueExpected(request)) {
					send100(ctx);
				}
				return viewBuilder.processHello(msg);
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

				send302(ctx, redirect.toString());
				return viewBuilder.processRedirect(msg);
			case "/status": 
				if (is100ContinueExpected(request)) {
					send100(ctx);
				}
				return viewBuilder.processStatus(msg);

			default:
				send404(ctx);
				return viewBuilder.error404(msg);
			}

		}	
		else {
			//TODO: if not HttpRequest
			return viewBuilder.def(msg);
		}
	}


	//	public void getView(ChannelHandlerContext ctx, Object msg) {
	//		
	//		HttpWettyServerChannel serverChannel = HttpWettyServerChannel.getParentServerChannel(ctx.channel());
	//		if (serverChannel != null) {  
	//			
	//			if (msg instanceof HttpRequest) {
	//	
	//				HttpRequest request = this.request = (HttpRequest) msg;
	//				
	//				//clear buffer
	//				buf.setLength(0);	
	//				//viewBuilder is responsible for parsing HTTP-request (msg), getting view and building HTTP-response
	//				buf.append(getViewByRequest(ctx, msg));
	//				appendDecoderResult(buf, request);
	//			}
	//	
	//			if (msg instanceof HttpContent) {
	//				appendEndOfContent(ctx, msg);
	//			}
	//		}
	//		//return result.toString();
	//	}

	//TODO: Move to ViewBuilder
	//let the view builder build the output
	public Object getViewByRequest(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {

			String url = request.getUri();
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
			String path = queryStringDecoder.path();

			//TODO: generalize
			switch (path) {
			case "/hello": 
				if (is100ContinueExpected(request)) {
					send100(ctx);
				}
				return viewBuilder.processHello(msg);
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

				send302(ctx, redirect.toString());
				return viewBuilder.processRedirect(msg);
			case "/status": 
				if (is100ContinueExpected(request)) {
					send100(ctx);
				}
				return viewBuilder.processStatus(msg);

			default:
				send404(ctx);
				return viewBuilder.error404(msg);
			}

		}	
		else {
			//if not HttpRequest
			return viewBuilder.def(msg);
		}
	}

	//TODO: Move to ViewBuilder
	@SuppressWarnings("unused")
	private void appendEndOfContent(ChannelHandlerContext ctx, Object msg) {

		//StringBuilder result = new StringBuilder();

		if (msg instanceof HttpContent) {
			//result.append(buf);

			HttpContent httpContent = (HttpContent) msg;

			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append("CONTENT: ");
				buf.append(content.toString(CharsetUtil.UTF_8));
				buf.append("\r\n");
				appendDecoderResult(buf, request);
			}

			if (msg instanceof LastHttpContent) {
				buf.append("END OF CONTENT\r\n");

				LastHttpContent trailer = (LastHttpContent) msg;
				if (!trailer.trailingHeaders().isEmpty()) {
					buf.append("\r\n");
					for (String name: trailer.trailingHeaders().names()) {
						for (String value: trailer.trailingHeaders().getAll(name)) {
							buf.append("TRAILING HEADER: ");
							buf.append(name).append(" = ").append(value).append("\r\n");
						}
					}
					buf.append("\r\n");
				}

				if (!writeResponse(trailer, ctx)) {
					// If keep-alive is off, close the connection once the content is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
		//return result.toString();
	}

	//TODO move to ViewBuilder
	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.getDecoderResult();
		if (result.isSuccess()) {
			return;
		}

		setDecoderFailureText(buf, result);
	}

	//TODO: move to ViewBuilder
	public static void setDecoderFailureText(StringBuilder buf,
			DecoderResult result) {
		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	//TODO move to ViewBuilder
	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		// Decide whether to close the connection or not.
		boolean keepAlive = isKeepAlive(request);

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
						Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

		setResponseHeaders(keepAlive, response);

		// Write the response.
		ctx.write(response);

		return keepAlive;
	}

	//TODO move to ViewBuilder
	public void setResponseHeaders(boolean keepAlive, FullHttpResponse response) {
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
			// Add keep alive header as per:
			// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		// Encode the cookie.
		String cookieString = request.headers().get(COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = CookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				for (Cookie cookie: cookies) {
					response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
				}
			}
		} else {
			// Browser sent no cookie.  Add some.
			response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
			response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
		}
	}

	//TODO move to ViewBuilder
	public static void send404(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
		ctx.write(response);
	}

	//TODO move to ViewBuilder
	public static void send100(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
		ctx.write(response);
	}

	//TODO move to ViewBuilder
	public static void send200(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		ctx.write(response);
	}

	//TODO move to ViewBuilder
	public static void send302(ChannelHandlerContext ctx, String redirect) {
		Channel channel = ctx.channel(); 

		HttpWettyServerChannel serverChannel = HttpWettyServerChannel.getParentServerChannel(channel);
		if (serverChannel != null) {    		
			Statistics statistics = serverChannel.getStatistics();
			statistics.gatherRedirect(redirect);
		}

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
		response.headers().set(LOCATION, redirect);
		ctx.write(response);
	}

}
