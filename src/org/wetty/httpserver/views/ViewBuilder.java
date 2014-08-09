package org.wetty.httpserver.views;

import static io.netty.handler.codec.http.HttpHeaders.getHost;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.wetty.httpserver.utils.Version;
import org.wetty.httpserver.utils.statistics.ChannelHolder;

public class ViewBuilder {
	//TODO:
	//private Object dataModel;
	
	public Object def(Object msg) {
		return "DEFAULT PAGE VIEW";
	}
	
	public Object error404(Object msg) {
		//TODO: send 404 header
		if (msg instanceof HttpRequest) {
			HttpRequest request = ((HttpRequest) msg);
			
		}
		
		return "404 PAGE VIEW";
	}

	public Object processHello(Object msg) {
		
		if (msg instanceof HttpRequest) {
			HttpRequest request = ((HttpRequest) msg);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String url = request.getUri();
			
			StringBuilder buf = new StringBuilder(); 
			buf.append("\"Hello World\"");
			
	        return buf.toString();
		}
	        else return def(msg);    	
	}


	public Object processRedirect(Object msg) {
		//TODO: Gather redirect statistics
		//TODO: Send 302 header
		
		if (msg instanceof HttpRequest) {
			HttpRequest request = ((HttpRequest) msg);
			
			String url = request.getUri();
			
			StringBuilder buf = new StringBuilder(); 
			buf.append("WELCOME TO THE ").append(Version.name()).append(" ").append(Version.version()).append("\r\n");
	        buf.append("===================================\r\n");
	
	        buf.append("ACTIVE CONNECTIONS: ").append(ChannelHolder.size()).append("\r\n");
	        buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
	        buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
	        buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");
	
	        HttpHeaders headers = request.headers();
	        if (!headers.isEmpty()) {
	            for (Map.Entry<String, String> h: headers) {
	                String key = h.getKey();
	                String value = h.getValue();
	                buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
	            }
	            buf.append("\r\n");
	        }
	
	        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
	        Map<String, List<String>> params = queryStringDecoder.parameters();
	        if (!params.isEmpty()) {
	            for (Entry<String, List<String>> p: params.entrySet()) {
	                String key = p.getKey();
	                List<String> vals = p.getValue();
	                for (String val : vals) {
	                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
	                }
	            }
	            buf.append("\r\n");
	        }
	        return buf.toString();
		}
	        else return def(msg);
	}

	
	public Object processStatus(Object msg) {
		//TODO: print status page and get results from SQLite db
		
		if (msg instanceof HttpRequest) {
			HttpRequest request = ((HttpRequest) msg);
			
			String url = request.getUri();
			
			StringBuilder buf = new StringBuilder(); 
			buf.append("WELCOME TO THE ").append(Version.name()).append(" ").append(Version.version()).append("\r\n");
	        buf.append("===================================\r\n");
	
	        buf.append("ACTIVE CONNECTIONS: ").append(ChannelHolder.size()).append("\r\n");
	        buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
	        buf.append("HOSTNAME: ").append(getHost(request, "unknown")).append("\r\n");
	        buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");
	
	        HttpHeaders headers = request.headers();
	        if (!headers.isEmpty()) {
	            for (Map.Entry<String, String> h: headers) {
	                String key = h.getKey();
	                String value = h.getValue();
	                buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
	            }
	            buf.append("\r\n");
	        }
	
	        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
	        Map<String, List<String>> params = queryStringDecoder.parameters();
	        if (!params.isEmpty()) {
	            for (Entry<String, List<String>> p: params.entrySet()) {
	                String key = p.getKey();
	                List<String> vals = p.getValue();
	                for (String val : vals) {
	                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
	                }
	            }
	            buf.append("\r\n");
	        }
	        return buf.toString();
		}
	        else return def(msg);
	}
}
