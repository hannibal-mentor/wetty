package org.wetty.httpserver.views;

import static io.netty.handler.codec.http.HttpHeaders.getHost;
import io.netty.handler.codec.http.HttpRequest;

import java.util.List;

import org.wetty.httpserver.utils.Version;
import org.wetty.httpserver.utils.statistics.ChannelHolder;
import org.wetty.httpserver.utils.statistics.StatisticsReader;

public class ViewBuilder {
	//TODO:
	//private Object dataModel;
	
	//default page
	public Object def(Object msg) {
		return "DEFAULT PAGE VIEW";
	}
	
	//404 page
	public Object error404(Object msg) {
		return "404 PAGE VIEW";
	}

	//page for /hello request
	public Object processHello(Object msg) {
		
		if (msg instanceof HttpRequest) {
			//HttpRequest request = ((HttpRequest) msg);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			StringBuilder buf = new StringBuilder(); 
			buf.append("\"Hello World\"\r\n");
			
	        return buf.toString();
		}
	        else return def(msg);    	
	}

	//page for /redirect request
	public Object processRedirect(Object msg) {
		if (msg instanceof HttpRequest) {
			//HttpRequest request = ((HttpRequest) msg);
			
			StringBuilder buf = new StringBuilder(); 
	        return buf.toString();
		}
	        else return def(msg);
	}
	
	//page for /status request
	public Object processStatus(Object msg) {
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
}
