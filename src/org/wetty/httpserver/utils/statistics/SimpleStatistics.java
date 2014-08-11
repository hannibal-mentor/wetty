
/*
 * Writes into DB and reads statistics
 * 
 */
package org.wetty.httpserver.utils.statistics;

import static io.netty.handler.codec.http.HttpHeaders.getHost;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.wetty.httpserver.server.HttpWettyServerTrafficHandler;
import org.wetty.httpserver.utils.HibernateUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.traffic.TrafficCounter;

public class SimpleStatistics implements Statistics {
	
	private static final int MILISECONDS_IN_SECOND = 1000;
	
	public void writeCounterData(Channel channel, TrafficCounter trafficCounter, String url) {

		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		
		synchronized (sessionFactory) {
			
			double speed = getSpeed(trafficCounter); 
			
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

	public double getSpeed(TrafficCounter trafficCounter) {
		double speed = 0.0;
		long interval = trafficCounter.lastTime() - trafficCounter.lastCumulativeTime();
		
		if (interval > 0) {
			speed = (trafficCounter.cumulativeReadBytes() + trafficCounter.cumulativeWrittenBytes())
					/ interval * MILISECONDS_IN_SECOND;
		} else return -1.0;
		return speed;
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

	@Override
	public void setChannelParameters(Channel channel, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			for (Entry<String, ChannelHandler> handler: channel.pipeline()) {
				if (handler.getValue() instanceof HttpWettyServerTrafficHandler) {
	
					HttpWettyServerTrafficHandler trafficHandler = (HttpWettyServerTrafficHandler) handler.getValue();
					synchronized (trafficHandler) {
						trafficHandler.setUrl(getHost(request, "unknown") + request.getUri());
					}
					break;
				}
			} 
		}
	}
}
