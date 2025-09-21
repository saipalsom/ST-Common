package org.saipal.common.service;

import java.util.ArrayList;
import java.util.List;

import org.saipal.common.entity.DataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DBService {

	@PersistenceContext
	protected EntityManager mysqlEntityManager;
	private static Logger LOGGER = LoggerFactory.getLogger(DBService.class);

	/**
	 * queries and returns the result list for the sql
	 * 
	 * @param sql sql query
	 * @return List<DataMap> containing the result set, data map key will be case
	 *         insensitive
	 * @return empty list if there is no matching tuple in database for given query
	 */
	@SuppressWarnings("unchecked")
	public List<DataMap> getResultList(String sql) {
		List<DataMap> data = new ArrayList<>();
		Query query = mysqlEntityManager.createNativeQuery(sql, Tuple.class);
		List<Tuple> resultList = query.getResultList();
		resultList.forEach((k) -> {
			data.add(tupleToMap(k));
		});
		return data;
	}

	private static DataMap tupleToMap(Tuple tuple) {

		DataMap map = new DataMap(false);
		if (tuple != null) {
			for (TupleElement<?> element : tuple.getElements()) {
				String alias = element.getAlias(); // Column name or alias
				Object value = tuple.get(alias); // Value of the column
				map.put(alias, value);
			}
		}
		return map;
	}
	/**
	 * queries and returns the result list for the sql uses prepared statement to
	 * execute the queries
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return List<DataMap> containing the result set, data map key will be case
	 *         insensitive
	 * @return empty list if there is no matching tuple in database for given query
	 */
	@SuppressWarnings("unchecked")
	public List<DataMap> getResultList(String sql, DataMap args) {
		LOGGER.debug("Sql args [{}]", args);
		List<DataMap> data = new ArrayList<>();
		Query query = createPreparedQuery(sql, args);
		List<Tuple> resultList = query.getResultList();
		resultList.forEach((k) -> {
			data.add(tupleToMap(k));
		});
		return data;
	}
	
	
	
	
	


	/**
	 * queries and returns the a single result for the sql uses prepared statement
	 * to execute the queries
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return datamap containing the single result, data map key will be case
	 *         insensitive
	 * @return null if there is no matching tuple for given query
	 */
	public DataMap getSingleResult(String sql, DataMap args) {
		LOGGER.debug("Sql args [{}]", args);
		Tuple tuple = null;
		try {
			Query query = createPreparedQuery(sql, args);
			tuple = (Tuple) query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return tupleToMap(tuple);

	}

	/**
	 * queries and returns the a single result for the sql
	 * 
	 * @param sql  sql query
	 * @param args arguments to be bound in runtime in the sql query
	 * @return Tuple containing the single tuple
	 * @return empty dataMap if there is no matching tuple for given query
	 */
	public DataMap getSingleResult(String sql) {
		Tuple tuple = null;
		try {
			Query query = mysqlEntityManager.createNativeQuery(sql, Tuple.class);
			tuple = (Tuple) query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
		return tupleToMap(tuple);

	}

//
	/**
	 * performs database update for given query(update/delete)
	 * 
	 * @param sql  sql query with dynamic parameter binding
	 * @param args arguments to be bound in runtime in the sql query
	 * @return Map contains number of rows affected in key "num" if update is
	 *         successful, else contains error message in "error" key
	 */

	public DataMap executeUpdate(String sql, DataMap args) {
		LOGGER.debug("Sql args [{}]", args);
		DataMap data = new DataMap();
		Query query = createPreparedQuery(sql, args);
		int rowAffected = query.executeUpdate();
		data.put("row_affected", rowAffected);
		return data;
	}

	/**
	 * 
	 * @param sql
	 * @return integer, number of rows affected by DDL statement
	 */
	public int executeUpdate(String sql) {
		Query query = mysqlEntityManager.createNativeQuery(sql, Tuple.class);
		return query.executeUpdate();

	}

	/**
	 * Helper method to create prepared query and set arguments
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	private Query createPreparedQuery(String sql, DataMap args) {
		Query query = mysqlEntityManager.createNativeQuery(sql, Tuple.class);
		args.forEach((k, v) -> {
			query.setParameter(k, v);
		});
		return query;
	}

}
