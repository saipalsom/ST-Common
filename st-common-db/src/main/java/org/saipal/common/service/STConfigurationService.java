package org.saipal.common.service;

import java.util.List;

import org.saipal.common.entity.DataMap;
import org.saipal.common.utility.STCollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class STConfigurationService implements InitializingBean {

	@Autowired
	private Environment env;
	private DataMap dbConfigs;
	@Autowired
	private DBService dbService;

	
	public String getProperty(String pPropertyKey) {
		String value = env.getProperty(pPropertyKey);
		if (value == null && !STCollectionUtils.isEmpty(dbConfigs)) {
			value = dbConfigs.getAsString(pPropertyKey);
		}
		return value;

	}

	public String getProperty(String pPropertyKey, String defaultValue) {
		String value = getProperty(pPropertyKey);
		return StringUtils.hasText(value) ? value : defaultValue;
	}

	public Integer getPropertyAsInt(String pPropertyKey) {
		return Integer.parseInt(getProperty(pPropertyKey));
	}

	public Integer getPropertyAsInt(String pPropertyKey, Integer defaultValue) {
		String val = getProperty(pPropertyKey);
		if (!StringUtils.hasText(val)) {
			return defaultValue;
		}
		return Integer.parseInt(val);
	}

	public Long getPropertyAsLong(String pPropertyKey) {
		return Long.parseLong(getProperty(pPropertyKey));
	}

	public Long getPropertyAsInt(String pPropertyKey, Long defaultValue) {
		String val = getProperty(pPropertyKey);
		if (!StringUtils.hasText(val)) {
			return defaultValue;
		}
		return Long.parseLong(val);
	}

	public Boolean getPropertyAsBoolean(String pPropertyKey) {
		return Boolean.parseBoolean(getProperty(pPropertyKey));
	}

	public Boolean getPropertyAsBoolean(String pPropertyKey, Boolean defaultValue) {
		String val = getProperty(pPropertyKey);
		if (!StringUtils.hasText(val)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(val);
	}

	public String[] getPropertyAsList(String pPropertyKey, String delimiter) {
		String val = getProperty(pPropertyKey);
		if (!StringUtils.hasText(val)) {
			return null;
		}
		return val.split(delimiter);
	}

	@Bean
	MessageSource messageSourceST() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages"); // Points to messages.properties
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	public String getApplicationQueueName() {
		return getProperty("spring.active.mq.queue.name");
	}

//to do
	public String getApplicationDomainName() {
		return getProperty("spring.app.domain.name");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		dbConfigs = new DataMap();
		List<DataMap> dbResult = dbService.getResultList("select * from st_config");
		if (!STCollectionUtils.isEmpty(dbResult)) {
			for (DataMap r : dbResult) {
				dbConfigs.put(r.getAsString("conf_name"), r.getAsString("conf_value"));
			}
		}

	}
}
