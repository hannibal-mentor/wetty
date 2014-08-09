
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.wetty.httpserver.utils.HibernateUtil;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	public synchronized void writeCounterData(Channel channel, TrafficCounter trafficCounter, String url) {
						
			SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		    Session session = sessionFactory.getCurrentSession();
				
		    Transaction tx = session.beginTransaction(); //TODO: while testing
		    //rolling back to save the test data
		         	        
		    Query query = session.createSQLQuery("INSERT INTO Requests (uri, src_ip, sent_bytes, received_bytes, speed) VALUES (?,?,?,?,?);")
		        	.setString(0, url)
		        	.setString(1, ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress().toString())
		        	.setBigInteger(2, BigInteger.valueOf(trafficCounter.currentReadBytes()))
		        	.setBigInteger(3, BigInteger.valueOf(trafficCounter.currentWrittenBytes()))
		        	.setLong(4,  trafficCounter.lastReadThroughput() + trafficCounter.lastWriteThroughput()); //why long?
    			;
			int result = query.executeUpdate();
			
			//tx.rollback(); // TODO: while testing
	        tx.commit();
			
	}

	@Override
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter, String url) {
		System.out.println(url + ", written: "+ trafficCounter.cumulativeWrittenBytes());
		System.out.println(url + ", read: "+ trafficCounter.cumulativeReadBytes());
		writeCounterData(channel, trafficCounter, url);
	}

	@Override
	public void gatherFromChannel(Channel channel) {
		if (channel.isActive()) {
			ChannelHolder.add(channel);
		} else {
			ChannelHolder.remove(channel);
		}
	}

	@Override
	public void gatherRedirect(String url) {
		writeRedirectData(url);
	}

	private synchronized void writeRedirectData(String url) {
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	    Session session = sessionFactory.getCurrentSession();
			
	    Transaction tx = session.beginTransaction();

	    Query query = session.createSQLQuery("INSERT INTO Redirects (url) VALUES (?);")
	        	.setString(0, url);
	    	
		int result = query.executeUpdate();
		
		//tx.rollback(); // TODO: while testing
        tx.commit();
	}
}
