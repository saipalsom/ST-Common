package org.saipal.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.saipal.common.configuration.DataSourceContextHolder;
import org.saipal.common.configuration.ReplicationRoutingDataSource.DataSourceType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(0)
public class ReadOnlyRouteInterceptor {
	@Around("@annotation(transactional)")
	public Object proceed(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {
		try {
			if (transactional.readOnly()) {
				DataSourceContextHolder.set(DataSourceType.SLAVE);
			} else {
				DataSourceContextHolder.set(DataSourceType.MASTER);
			}
			return proceedingJoinPoint.proceed();
		} finally {
			DataSourceContextHolder.clear();
		}
	}
}
