package org.saipal.common.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	private static final String PRIMARY_DATASOURCE_PREFIX = "spring.datasource";

	@Autowired
	private Environment environment;

	@Bean
	@Primary
	DataSource primaryDataSource() {
		ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
		DataSource primaryDataSource = buildDataSource("PrimaryHikariPool", PRIMARY_DATASOURCE_PREFIX);
		Map<String, DataSource> slaves = loadAllSlaves(primaryDataSource);

		// Combine all
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put("master", primaryDataSource);
		List<String> slaveKeys = new ArrayList<>();

		for (Map.Entry<String, DataSource> entry : slaves.entrySet()) {
			targetDataSources.put(entry.getKey(), entry.getValue());
			slaveKeys.add(entry.getKey());
		}
		routingDataSource.setDefaultTargetDataSource(primaryDataSource);
		routingDataSource.setTargetDataSourcesWithSlaves(targetDataSources, slaveKeys);
		return routingDataSource;
	}

	private DataSource buildDataSource(String poolName, String dataSourcePrefix) {
		final HikariConfig hikariConfig = new HikariConfig();

		hikariConfig.setPoolName(poolName);
		hikariConfig.setJdbcUrl(environment.getProperty(String.format("%s.jdbcUrl", dataSourcePrefix)));
		hikariConfig.setUsername(environment.getProperty(String.format("%s.username", dataSourcePrefix)));
		hikariConfig.setPassword(environment.getProperty(String.format("%s.password", dataSourcePrefix)));
		hikariConfig.setDriverClassName(environment.getProperty(String.format("%s.driverClassName", dataSourcePrefix)));

		return new HikariDataSource(hikariConfig);
	}

	public Map<String, DataSource> loadAllSlaves(DataSource masterDataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(masterDataSource);
		Map<String, DataSource> slaveDataSources = new HashMap<>();
		List<Map<String, Object>> configs = jdbcTemplate.queryForList("SELECT * FROM slave_db_config");
		int i = 0;
		for (Map<String, Object> config : configs) {
			HikariConfig hikariConfig = new HikariConfig();

			hikariConfig.setJdbcUrl((String) config.get("url"));
			hikariConfig.setUsername((String) config.get("username"));
			hikariConfig.setPassword((String) config.get("password"));
			hikariConfig.setDriverClassName(
					config.get("driver") != null ? (String) config.get("driver") : "com.mysql.cj.jdbc.Driver");
			HikariDataSource dataSource = new HikariDataSource(hikariConfig);
			slaveDataSources.put("Slave" + i, dataSource);
			i++;
		}
		return slaveDataSources;
	}

}
