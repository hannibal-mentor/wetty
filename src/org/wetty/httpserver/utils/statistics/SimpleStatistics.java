
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import java.math.BigInteger;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.wetty.httpserver.utils.HibernateUtil;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	public synchronized void write(Channel channel, TrafficCounter trafficCounter) {
						
				SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		        Session session = sessionFactory.getCurrentSession();
				
		        Transaction tx = session.beginTransaction(); //TODO: while testing
		        //rolling back to save the test data
;		         	        
		        Query query = session.createSQLQuery("INSERT INTO Requests (uri, src_ip, sent_bytes, received_bytes, speed) VALUES (?,?,?,?,?);")
		        	.setString(0, channel.localAddress().toString())
		        	.setString(1, channel.remoteAddress().toString())
		        	.setBigInteger(2, BigInteger.valueOf(trafficCounter.currentReadBytes()))
		        	.setBigInteger(3, BigInteger.valueOf(trafficCounter.currentWrittenBytes()))
		        	.setLong(4,  trafficCounter.lastReadThroughput() + trafficCounter.lastWriteThroughput()); //why long?
    			;
			int result = query.executeUpdate();
			
			//tx.rollback(); // TODO: while testing
	        tx.commit();
			
			 //closing hibernate resources
	        //sessionFactory.close(); //TODO: close appropriately
	}

	@Override
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter) {
		System.out.println(channel.remoteAddress().toString() + ", written: "+ trafficCounter.cumulativeWrittenBytes());
		write(channel, trafficCounter);
	}

	@Override
	public void gatherFromChannel(Channel channel) {
		//TODO: Store remote port
        //System.out.println(ctx.channel().remoteAddress());
        //String host = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
        //int port = ((InetSocketAddress)ctx.getChannel().getRemoteAddress()).getPort();
		
		if (channel.isActive()) {
			System.out.println("Channel is active "+channel.toString());
			ChannelHolder.add(channel);
		} else {
			System.out.println("Channel is not active "+channel.toString());
			ChannelHolder.remove(channel);
		}
	}

	@Override
	public void gatherRedirect(Channel channel, String url) {
		// TODO Auto-generated method stub
		
	}
}
