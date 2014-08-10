package org.wetty.httpserver.utils.statistics;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.wetty.httpserver.utils.HibernateUtil;

public class StatisticsReader {

	//- общее количество запросов SELECT count(*) from Requests
    //- количество уникальных запросов (по одному на IP) SELECT count(DISTINCT src_ip) from Requests
    //- счетчик запросов на каждый IP в виде таблицы с колонкам и IP, кол-во запросов, время последнего запроса
    //- select src_ip as IP, count(*) as request_count, max(timestamp) as last_request_time	
    //- количество переадресаций по url'ам в виде таблицы, с колонками url, кол-во переадресация
    //- select url, count(*) as number_of_redirects
    //- в виде таблицы лог из 16 последних обработанных соединений, колонки src_ip, URI, timestamp, sent_bytes, received_bytes, speed (bytes/sec)
    //- select src_ip, uri, timestamp, sent_bytes, received_bytes, speed order by id DESC LIMIT 16

	
	public List<Object> getAllRequests() {
		return getResultList("SELECT count(*) from Requests;");
	}

	public List<Object> getUniqueRequestsGroupedByIP() {
		return getResultList("SELECT src_ip as IP, count(DISTINCT src_ip) as number from Requests GROUP BY src_ip;");
	}
	
	public List<Object> getRequestDetails() {
		return getResultList("SELECT src_ip as IP, count(*) as request_count, max(datetime(timestamp,'localtime')) as last_request_time "+
				"from Requests GROUP BY src_ip;");
	}
	
	public List<Object> getRedirects() {
		return getResultList("SELECT url, count(*) as number_of_redirects from Redirects GROUP BY url;");
	}
	
	public List<Object> getLastConnections() {
		return getResultList("SELECT src_ip, uri, datetime(timestamp,'localtime'), sent_bytes, received_bytes, speed from Requests order by id DESC LIMIT 16;");
	}
	
	@SuppressWarnings("unchecked")
	public synchronized List<Object> getResultList(String queryText) {
		List<Object> result = null;
		
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		synchronized (sessionFactory) {
		try {
			sessionFactory.getCurrentSession().beginTransaction();

			Query query = sessionFactory.getCurrentSession().createSQLQuery(queryText);
			result = query.list(); 
			
		    sessionFactory.getCurrentSession().getTransaction().commit();
		}
		catch (RuntimeException e) {
			sessionFactory.getCurrentSession().getTransaction().rollback();
			System.out.println("Problem reading");
		    throw e; // or display error message
		}
		finally {
			sessionFactory.getCurrentSession().close();
		}
		}
		return result;
	}
	
}
