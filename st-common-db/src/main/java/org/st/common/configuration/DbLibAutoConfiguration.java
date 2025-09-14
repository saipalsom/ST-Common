package org.st.common.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.st.common")
//@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class DbLibAutoConfiguration {

}
