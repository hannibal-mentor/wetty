package org.wetty.httpserver.views;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.util.List;
import org.wetty.httpserver.utils.Version;
import org.wetty.httpserver.utils.statistics.ChannelHolder;
import org.wetty.httpserver.utils.statistics.StatisticsReader;

public class HTMLViewBuilder extends ViewBuilder {

	//default page
	public Object def(Object msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(doctype()).append("<html><body>DEFAULT PAGE VIEW</body></html>");
		return sb.toString();
	}

	//page for /hello request
	public Object processHello(ChannelHandlerContext ctx, Object msg) {
		if (is100ContinueExpected(getRequest())) {
			send100(ctx);
		}
		if (msg instanceof HttpRequest) {

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			StringBuilder buf = new StringBuilder(); 
			buf.append(doctype()).append("<html></body>\"Hello World\"</body></html>");

			return buf.toString();
		}
		else return def(msg);    	
	}

	//page for /status request
	public Object processStatus(ChannelHandlerContext ctx, Object msg) {
		if (is100ContinueExpected(getRequest())) {
			send100(ctx);
		}

		//print status page and get results from SQLite db
		if (msg instanceof HttpRequest) {
			
			StringBuilder buf = new StringBuilder();

			buf.append(doctype()).append("<html><body>\r\n");

			buf.append("<h1>WELCOME TO THE ").append(Version.name()).append(" ").append(Version.version()).append("</h1>\r\n");
			buf.append("<hr>\r\n");

			buf.append("<h4>ACTIVE CONNECTIONS: ").append(ChannelHolder.size()).append("</h4>\r\n");
			
			StatisticsReader sr = new StatisticsReader();
			appendOneColumnSection(buf, sr.getAllRequests(), "TOTAL REQUESTS: ");

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

		buf.append("<h4>").append(header).append("</h4>").append("\r\n");
		buf.append("<table border = \"1\">\r\n<tr>\r\n");
		
		for (String element: firstRow) {
			buf.append("<td>").append(element).append("</td>\r\n");
		}
		buf.append("</tr>").append("\r\n");

		for (Object obj: collection) {
			buf.append("<tr>\r\n<td>\r\n").append(++rowCounter).append("</td>\r\n");
			for (Object rowObj: (Object [])obj) {
				buf.append("<td>").append(rowObj).append("</td>\r\n");
			};
			buf.append("</td>\r\n");
		}
		buf.append("</tr>").append("</table>\r\n<br>\r\n");
	}

	public void appendOneColumnSection(StringBuilder buf, List<Object> collection, String header) {

		for (Object obj: collection) {
			buf.append("<h4>").append(header).append(obj.toString()).append("</h4>\r\n");
		}
	}

	public void appendEndOfContent(StringBuilder buf, ChannelHandlerContext ctx, Object msg) {

		if (msg instanceof HttpContent) {

			HttpContent httpContent = (HttpContent) msg;

			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append("CONTENT: ");
				buf.append(content.toString(CharsetUtil.UTF_8));
				buf.append("<br>");
				appendDecoderResult(buf, getRequest());
			}

			if (msg instanceof LastHttpContent) {
				//buf.append("END OF CONTENT<br>");

				LastHttpContent trailer = (LastHttpContent) msg;
				if (!trailer.trailingHeaders().isEmpty()) {
					buf.append("<br>");
					for (String name: trailer.trailingHeaders().names()) {
						for (String value: trailer.trailingHeaders().getAll(name)) {
							buf.append("TRAILING HEADER: ");
							buf.append(name).append(" = ").append(value).append("<br>");
						}
					}
					buf.append("<br>");
				}

				buf.append("</body></html>");
				if (!super.writeResponse(buf, trailer, ctx)) {
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
		buf.append(doctype()).append("<html><body>.. WITH DECODER FAILURE: </body></html>");
		buf.append(result.cause());
	}

	public void setResponseHeaders(boolean keepAlive, FullHttpResponse response) {
		super.setResponseHeaders(keepAlive, response);
		response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
	}

	static String doctype() {
		return "<!DOCTYPE html>\r\n";
	}

}
