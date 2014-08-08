package io.netty.example.discard;

import java.net.SocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side channel.
 */
public class DiscardServerHandler extends ChannelHandlerAdapter { 

	 @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		 ByteBuf in = (ByteBuf) msg;
		    try {
		    	StringBuilder sb = new StringBuilder();
		    	
		        while (in.isReadable()) {
		        	sb.append((char) in.readByte());
		        }
		        
		        ctx.write(getResponse(sb.toString()));
		       
		        ctx.flush();
		        
		    } finally {
		        ReferenceCountUtil.release(msg);
		    }
	    }

	
    @Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		DiscardServer.decrementActiveConnections();
	}


	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		DiscardServer.incrementActiveConnections();
	}


	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		// TODO Auto-generated method stub
		super.close(ctx, promise);
	}


	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise)
			throws Exception {
		// TODO Auto-generated method stub
		super.connect(ctx, remoteAddress, localAddress, promise);
		//DiscardServer.incrementActiveConnections();
	}


	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise)
			throws Exception {
		// TODO Auto-generated method stub
		super.disconnect(ctx, promise);
		DiscardServer.decrementActiveConnections();
	}


	private Object getResponse2(String requestText) {
    	String pageText = "<html><body><a href=\"http://example.com/about.html#contacts\">Click here</a></body></html>";
    	
		return  Unpooled.copiedBuffer( "HTTP/1.x 301 Moved Permanently\n"
	     		+ "Location: http://example.com/about.html#contacts\n"
	     		+ "Date: Thu, 19 Feb 2009 11:08:01 GMT\n"
	     		+ "Server: Apache/2.2.4\n"
	     		+ "Content-Type: text/html; charset=windows-1251\n"
	     		+ "Content-Length: "+pageText.length()+"\n"
	     		+ "\n"
	     		+ pageText,
	             CharsetUtil.UTF_8) ;
	}
    
    private Object getResponse(String requestText) {
    	String pageText = "<html><body>Active connections: "+DiscardServer.getActiveConnections()+"</body></html>";
    	
		return  Unpooled.copiedBuffer( "HTTP/1.1 200 OK\n"
	    		+ "Date: Thu, 19 Feb 2009 11:08:01 GMT\n"
	     		+ "Server: Wetty/0.0.1\n"
	     		+ "Content-Type: text/html; charset=utf-8\n"
	     		+ "Content-Length: "+pageText.length()+"\n"
	     		//+ "Connection: close"+"\n"
	     		+ "\n"
	     		+ pageText + "\n",
	             CharsetUtil.UTF_8) ;
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
		//IOException when browser is closed
		
        cause.printStackTrace();
        ctx.close();
    }
}