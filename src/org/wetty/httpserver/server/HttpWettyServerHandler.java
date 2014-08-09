    /*
     * Based on Netty examples
     */    

package org.wetty.httpserver.server;
    

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.Version;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
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
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.wetty.httpserver.utils.statistics.ChannelGatherable;
import org.wetty.httpserver.utils.statistics.ChannelHolder;
import org.wetty.httpserver.views.ViewBuilder;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
    
    public class HttpWettyServerHandler extends SimpleChannelInboundHandler<Object> implements ChannelGatherable {
    
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

		private HttpRequest request;

        private final StringBuilder buf = new StringBuilder();
    
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
			if (channel.parent() instanceof HttpWettyServerChannel) {
				((HttpWettyServerChannel) channel.parent()).getStatistics().gatherFromChannel(channel);
			}			
		}

		@Override
		public void channelUnregistered(ChannelHandlerContext ctx)
				throws Exception {
			super.channelUnregistered(ctx);
			gatherStatistics(ctx.channel());		
		}
		
		@Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
			gatherStatistics(ctx.channel());
						
            if (msg instanceof HttpRequest) {
            	
            	HttpRequest request = this.request = (HttpRequest) msg;
                
            	//TODO Refactor
                //Setting URI for statistics
                for (Entry<String, ChannelHandler> handler: ctx.channel().pipeline()) {
                	if (handler.getValue() instanceof HttpWettyServerTrafficHandler) {
                		
                		HttpWettyServerTrafficHandler trafficHandler = (HttpWettyServerTrafficHandler) handler.getValue();
                		synchronized (trafficHandler) {
                			trafficHandler.setUrl(getHost(request, "unknown") + request.getUri());
                		}
                		break;
                	}
                }          
                
                if (ctx.channel().parent() instanceof HttpWettyServerChannel) {
            		
            		HttpWettyServerChannel serverChannel = (HttpWettyServerChannel) ctx.channel().parent();
            		ControllerManager controllerManager = serverChannel.getControllerManager();
            		ViewBuilder viewBuilder = serverChannel.getViewBuilder();
            		
            		//TODO: parse HTTP-request (msg), get view and return HTTP-response
            		
                    buf.setLength(0);
                    buf.append((String) controllerManager.getView(viewBuilder, ctx, request));
                   
                    appendDecoderResult(buf, request);
            	}
            }
    
            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
    
//                if (ctx.channel().parent() instanceof HttpWettyServerChannel) {
//                	
//                	HttpWettyServerChannel serverChannel = (HttpWettyServerChannel) ctx.channel().parent();
//            		ControllerManager controllerManager = serverChannel.getControllerManager();
//            		ViewBuilder viewBuilder = serverChannel.getViewBuilder();
//                }
                
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
    
       
  
      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
          cause.printStackTrace();
          ctx.close();
          
          gatherStatistics(ctx.channel());
       }
      
  }