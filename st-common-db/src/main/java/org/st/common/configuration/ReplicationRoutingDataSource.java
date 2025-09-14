package org.st.common.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

	private List<String> slaveDataSourceKeys;
	private AtomicInteger counter = new AtomicInteger(0);

	public void setSlaveDataSourceKeys(List<String> slaveKeys) {
		this.slaveDataSourceKeys = slaveKeys;
	}

	public void setTargetDataSourcesWithSlaves(Map<Object, Object> targetDataSources, List<String> slaveKeys) {
		super.setTargetDataSources(targetDataSources);
		if (this.slaveDataSourceKeys != null) {
			this.slaveDataSourceKeys.clear();
		} else {
			this.slaveDataSourceKeys = new ArrayList<>();
		}
		this.slaveDataSourceKeys.addAll(slaveKeys);
		afterPropertiesSet(); // Important to re-initialize
	}
	@Override
	protected Object determineCurrentLookupKey() {
		if (DataSourceContextHolder.get() == null || DataSourceContextHolder.get() == DataSourceType.MASTER
				|| slaveDataSourceKeys == null || slaveDataSourceKeys.isEmpty()) {
			return "master";
		}

		String existing = DataSourceContextHolder.getSlaveKey();
		if (existing != null) {
			return existing;
		}
		// Round robin logic for slave
		int idx = counter.getAndUpdate(i -> (i + 1) % slaveDataSourceKeys.size());
		String selectedKey = slaveDataSourceKeys.get(idx);
		DataSourceContextHolder.setSlaveKey(selectedKey);
		return selectedKey;

	}

	public enum DataSourceType {
		MASTER, SLAVE
	}
}
