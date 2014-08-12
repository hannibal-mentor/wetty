package org.wetty.httpserver.views;

import static io.netty.handler.codec.http.HttpHeaders.getHost;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.wetty.httpserver.utils.AttributeClassSpawner;
import org.wetty.httpserver.utils.Version;
import org.wetty.httpserver.utils.statistics.ChannelHolder;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.utils.statistics.StatisticsReader;

public class ViewBuilder {
	//MODEL:
	//Object msg
	//ChannelHandlerContext ctx
	private HttpRequest request;

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	//default page
	public Object def(Object msg) {
			return "DEFAULT PAGE VIEW";
	}
	
	//404 page
	public Object error404(ChannelHandlerContext ctx, Object msg) {
		send404(ctx);
		return "404 PAGE VIEW";
	}

	//page for /hello request
	public Object processHello(ChannelHandlerContext ctx, Object msg) {
		if (is100ContinueExpected(request)) {
			send100(ctx);
		}
		if (msg instanceof HttpRequest) {

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			StringBuilder buf = new StringBuilder(); 
			buf.append("\"Hello World\"\r\n");

			return buf.toString();
		}
		else return def(msg);    	
	}

	//page for /redirect request
	public Object processRedirect(ChannelHandlerContext ctx, Object msg) {
		StringBuilder redirect = new StringBuilder(); 
		
		String url = request.getUri();
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(url);
		
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

		if (msg instanceof HttpRequest) {
			//HttpRequest request = ((HttpRequest) msg);

			StringBuilder buf = new StringBuilder(); 
			return buf.toString();
		}
		else return def(msg);
	}

	//page for /status request
	public Object processStatus(ChannelHandlerContext ctx, Object msg) {
		if (is100ContinueExpected(request)) {
			send100(ctx);
		}

		//print status page and get results from SQLite db
		if (msg instanceof HttpRequest) {
			HttpRequest request = ((HttpRequest) msg);

			StringBuilder buf = new StringBuilder(); 
			buf.append("WELCOME TO THE ").append(Version.name()).append(" ").append(Version.version()).append("\r\n");
			buf.append("===================================\r\n");

			buf.append("ACTIVE CONNECTIONS: ").append(ChannelHolder.size()).append("\r\n");
			buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
			buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
			buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");

			StatisticsReader sr = new StatisticsReader();
			appendOneColumnSection(buf, sr.getAllRequests(), "TOTAL REQUESTS: ", new String[]{"#", "url", "number"});

			appendSection(buf, sr.getRedirects(), "REDIRECTS:", new String[]{"#", "url", "number"});
			appendSection(buf, sr.getUniqueRequestsGroupedByIP(), "UNIQUE REQUESTS BY ID:", new String[]{"#", "IP", "number"});
			appendSection(buf, sr.getRequestDetails(), "REQUEST DETAILS:", new String[]{"#", "IP", "request_count", "last_request_time"});	        
			appendSection(buf, sr.getLastConnections(), "LAST 16 CONNECTIONS:", new String[]{"#", "src_ip", "uri", "timestamp", "sent_bytes", "received_bytes", "speed"});

			return buf.toString();
		}
		else return def(msg);
	}

	public void appendSection(StringBuilder buf, List<Object> collection, String header, String [] firstRow) {
		int rowCounter = 0;

		buf.append(header).append("\r\n");

		for (String element: firstRow) {
			buf.append(element).append("|");
		}
		buf.append("\r\n");

		for (Object obj: collection) {
			buf.append(++rowCounter).append("|");
			for (Object rowObj: (Object [])obj) {
				buf.append(rowObj).append("|");
			};
			buf.append("\r\n");
		}
		buf.append("\r\n");
	}

	public void appendOneColumnSection(StringBuilder buf, List<Object> collection, String header, String [] firstRow) {

		for (Object obj: collection) {
			buf.append(header).append(obj.toString()).append("\r\n\r\n");
		}
	}

	public void appendEndOfContent(StringBuilder buf, ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof HttpContent) {

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

				if (!writeResponse(buf, trailer, ctx)) {
					// If keep-alive is off, close the connection once the content is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}

	public void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.getDecoderResult();
		if (result.isSuccess()) {
			return;
		}

		setDecoderFailureText(buf, result);
	}

	public static void setDecoderFailureText(StringBuilder buf,	DecoderResult result) {
		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(StringBuilder buf, HttpObject currentObj, ChannelHandlerContext ctx) {
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

	public static void send404(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
		ctx.write(response);
	}

	public static void send100(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
		ctx.write(response);
	}

	public static void send200(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
		ctx.write(response);
	}

	public static void send302(ChannelHandlerContext ctx, String redirect) {
		Channel channel = ctx.channel(); 

		Statistics statistics = AttributeClassSpawner.createStatisticsClass(channel);
		statistics.gatherRedirect(redirect);

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
		response.headers().set(LOCATION, redirect);
		ctx.write(response);
	}
}
