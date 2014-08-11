/*
 * Based on Netty examples
 */    

package org.wetty.httpserver.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.wetty.httpserver.controllers.ControllerManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.Set;

import org.wetty.httpserver.utils.statistics.ChannelGatherable;
import org.wetty.httpserver.utils.statistics.Statistics;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class HttpWettyServerHandler extends SimpleChannelInboundHandler<Object> implements ChannelGatherable {

	private final StringBuilder buf = new StringBuilder();
	private HttpRequest request;

	//demonstrating reflection
	private Class<? extends HttpWettyServerChannel> serverChannelClass = HttpWettyServerChannel.class;

	public void setServerChannelClass(Class<? extends HttpWettyServerChannel> serverChannelClass) {
		this.serverChannelClass = serverChannelClass;
	}

	public Class<? extends HttpWettyServerChannel> getServerChannelClass() {
		return serverChannelClass;
	}

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
		HttpWettyServerChannel serverChannel = getParentServerChannel(channel);
		if (serverChannel != null) {   
			serverChannel.getStatistics().gatherFromChannel(channel);
		}		
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx)
			throws Exception {
		super.channelUnregistered(ctx);
		gatherStatistics(ctx.channel());		
	}

	//	@Override
	//	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
	//		Channel channel = ctx.channel();
	//		gatherStatistics(channel);
	//
	//		//Initializing statistics request parameters parameters on channel read and handling the request	
	//		HttpWettyServerChannel serverChannel = getParentServerChannel(channel);
	//		
	//		if (serverChannel != null) {    		
	//
	//			ControllerManager controllerManager = serverChannel.getControllerManager();
	//
	//			if (msg instanceof HttpRequest) {
	//				Statistics statistics = serverChannel.getStatistics();
	//				statistics.setChannelParameters(channel, msg);
	//
	//				//clean buffer
	//				//buf.setLength(0);	
	//			}
	//			
	//			if (msg instanceof HttpContent || msg instanceof HttpRequest) {
	//				//buf.append((String) controllerManager.getView(ctx, msg, buf));
	//				controllerManager.getView(ctx, msg);
	//			}
	//		}
	//	}

	//getting parent server channel with viewbuilder, controllermanager and statistics
	//could be overriden
	public HttpWettyServerChannel getParentServerChannel(Channel channel) {
		//by default HttpWettyServerChannel serverChannel = HttpWettyServerChannel.getParentServerChannel(channel);

		Method method = null;
		try {
			method = serverChannelClass.getMethod("getParentServerChannel", Channel.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace(); //let's just leave it
		} catch (SecurityException e) {
			e.printStackTrace(); //let's just leave it
		}
		HttpWettyServerChannel serverChannel = null;
		try {
			serverChannel = (HttpWettyServerChannel) method.invoke(null, channel);
		} catch (IllegalAccessException e) {
			e.printStackTrace(); //let's just leave it
		} catch (IllegalArgumentException e) {
			e.printStackTrace(); //let's just leave it
		} catch (InvocationTargetException e) {
			e.printStackTrace(); //let's just leave it
		}
		return serverChannel;
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
		gatherStatistics(channel);

		//Initializing statistics request parameters parameters on channel read and handling the request	
		HttpWettyServerChannel serverChannel = getParentServerChannel(channel);

		if (serverChannel != null) {    		

			ControllerManager controllerManager = serverChannel.getControllerManager();

			if (msg instanceof HttpRequest) {
				Statistics statistics = serverChannel.getStatistics();
				statistics.setChannelParameters(channel, msg);
			}

			if (msg instanceof HttpRequest) {

				HttpRequest request = this.request = (HttpRequest) msg;

				Statistics statistics = serverChannel.getStatistics();
				statistics.setChannelParameters(channel, msg);

				if (ctx.channel().parent() instanceof HttpWettyServerChannel) {

					//TODO: parse HTTP-request (msg), get view and return HTTP-response

					buf.setLength(0);
					buf.append((String) controllerManager.getView(ctx, request));

					appendDecoderResult(buf, request);
				}
			}

			if (msg instanceof HttpContent) {
				HttpContent httpContent = (HttpContent) msg;

				//TODO: Move to ViewBuilder
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
		}
	}

	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.getDecoderResult();
		if (result.isSuccess()) {
			return;
		}

		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		// Decide whether to close the connection or not.
		boolean keepAlive = isKeepAlive(request);
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, currentObj.getDecoderResult().isSuccess()? OK : BAD_REQUEST,
						Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

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

		// Write the response.
		ctx.write(response);

		return keepAlive;
	}

}