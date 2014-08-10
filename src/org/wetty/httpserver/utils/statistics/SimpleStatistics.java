
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import java.math.BigInteger;
import java.net.InetSocketAddress;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.wetty.httpserver.utils.HibernateUtil;

import io.netty.channel.Channel;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	
	private static final int MILISECONDS_IN_SECOND = 1000;
	
	public synchronized void writeCounterData(Channel channel, TrafficCounter trafficCounter, String url) {

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		
		synchronized (sessionFactory) {
			
			double speed = (trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes()) * MILISECONDS_IN_SECOND
						/ (trafficCounter.lastTime() - trafficCounter.lastCumulativeTime());
			System.out.println(speed);
			try {
				sessionFactory.getCurrentSession().beginTransaction();
	
				Query query = sessionFactory.getCurrentSession().createSQLQuery("INSERT INTO Requests (uri, src_ip, sent_bytes, received_bytes, speed) VALUES (?,?,?,?,?);")
						.setString(0, url)
						.setString(1, ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress().toString())
						.setBigInteger(2, BigInteger.valueOf(trafficCounter.cumulativeReadBytes()))
						.setBigInteger(3, BigInteger.valueOf(trafficCounter.cumulativeWrittenBytes()))
						.setDouble(4,  speed);
	
						@SuppressWarnings("unused")
						int result = query.executeUpdate();
						sessionFactory.getCurrentSession().getTransaction().commit();
			}
			catch (RuntimeException e) {
				sessionFactory.getCurrentSession().getTransaction().rollback();
				System.out.println("Problem writing stats");
				throw e; // or display error message
			}
			finally {
				sessionFactory.getCurrentSession().close();
			}
		}
	}

	@Override
	public void gatherFromTrafficCounter(Channel channel, TrafficCounter trafficCounter, String url) {
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

	private void writeRedirectData(String url) {

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		synchronized (sessionFactory) {
		try {
			sessionFactory.getCurrentSession().beginTransaction();

			Query query = sessionFactory.getCurrentSession().createSQLQuery("INSERT INTO Redirects (url) VALUES (?);")
					.setString(0, url);

			@SuppressWarnings("unused")
			int result = query.executeUpdate();
			sessionFactory.getCurrentSession().getTransaction().commit();
		}
		catch (RuntimeException e) {
			sessionFactory.getCurrentSession().getTransaction().rollback();
			System.out.println("Problem writing stats");
			throw e; // or display error message
		}
		finally {
			sessionFactory.getCurrentSession().close();
		}
		}
	}
}
