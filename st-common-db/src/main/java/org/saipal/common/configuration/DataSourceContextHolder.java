package org.saipal.common.configuration;

import org.saipal.common.configuration.ReplicationRoutingDataSource.DataSourceType;

public class DataSourceContextHolder {
	private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();
	private static final ThreadLocal<String> slaveKeyHolder = new ThreadLocal<>();

	public static void set(DataSourceType type) {
		contextHolder.set(type);
	}

	public static DataSourceType get() {
		return contextHolder.get();
	}

	public static void setSlaveKey(String key) {
		slaveKeyHolder.set(key);
	}

	public static String getSlaveKey() {
		return slaveKeyHolder.get();
	}

	public static void clear() {
		contextHolder.remove();
		slaveKeyHolder.remove();
	}
}
