
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.wetty.httpserver.utils.HibernateUtil;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	public void write() {
		// TODO: Use hibernate
						
				SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		        Session session = sessionFactory.getCurrentSession();
				
		        //Transaction tx = session.beginTransaction();
		        //rolling back to save the test data
		        //tx.rollback();
		         
		       
		
		        Query query = session.createQuery("insert into Stock(stock_code, stock_name)" +
    			"select stock_code, stock_name from backup_stock");
			int result = query.executeUpdate();
			
			 //closing hibernate resources
	        sessionFactory.close();
	}

	@Override
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter) {
		System.out.println(channel.remoteAddress().toString() + ", written: "+ trafficCounter.cumulativeWrittenBytes());
		write();
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
}
