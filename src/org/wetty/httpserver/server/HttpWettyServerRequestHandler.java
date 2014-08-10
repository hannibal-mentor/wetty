package org.wetty.httpserver.server;

public class HttpWettyServerRequestHandler implements RequestHandler {

	@Override
	public void handleRequest(Object msg) {
		//		 if (msg instanceof HttpRequest) {
		//         	
		//         	HttpRequest request = this.request = (HttpRequest) msg;
		//             //TODO: parse HTTP-request (msg), get view and return HTTP-response
		//             
		//             if (is100ContinueExpected(request)) {
		//                 send100Continue(ctx);
		//             }
		// 
		//             buf.setLength(0);
		//             buf.append("WELCOME TO THE ").append(Version.name()).append(" ").append(Version.version()).append("\r\n");
		//             buf.append("===================================\r\n");
		// 
		//             buf.append("ACTIVE CONNECTIONS: ").append(ChannelHolder.size()).append("\r\n");
		//             buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
		//             buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
		//             buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");
		// 
		//             HttpHeaders headers = request.headers();
		//             if (!headers.isEmpty()) {
		//                 for (Map.Entry<String, String> h: headers) {
		//                     String key = h.getKey();
		//                     String value = h.getValue();
		//                     buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
		//                 }
		//                 buf.append("\r\n");
		//             }
		// 
		//             QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
		//             Map<String, List<String>> params = queryStringDecoder.parameters();
		//             if (!params.isEmpty()) {
		//                 for (Entry<String, List<String>> p: params.entrySet()) {
		//                     String key = p.getKey();
		//                     List<String> vals = p.getValue();
		//                     for (String val : vals) {
		//                         buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
		//                     }
		//                 }
		//                 buf.append("\r\n");
		//             }
		// 
		//             appendDecoderResult(buf, request);
		//         }
		// 
		//         if (msg instanceof HttpContent) {
		//             HttpContent httpContent = (HttpContent) msg;
		// 
		//             ByteBuf content = httpContent.content();
		//             if (content.isReadable()) {
		//                 buf.append("CONTENT: ");
		//                 buf.append(content.toString(CharsetUtil.UTF_8));
		//                 buf.append("\r\n");
		//                 appendDecoderResult(buf, request);
		//             }
		// 
		//             if (msg instanceof LastHttpContent) {
		//                 buf.append("END OF CONTENT\r\n");
		// 
		//                 LastHttpContent trailer = (LastHttpContent) msg;
		//                 if (!trailer.trailingHeaders().isEmpty()) {
		//                     buf.append("\r\n");
		//                     for (String name: trailer.trailingHeaders().names()) {
		//                         for (String value: trailer.trailingHeaders().getAll(name)) {
		//                             buf.append("TRAILING HEADER: ");
		//                             buf.append(name).append(" = ").append(value).append("\r\n");
		//                         }
		//                     }
		//                     buf.append("\r\n");
		//                 }
		// 
		//                 if (!writeResponse(trailer, ctx)) {
		//                     // If keep-alive is off, close the connection once the content is fully written.
		//                     ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		//                 }
		//             }
		//         }

	}

}
