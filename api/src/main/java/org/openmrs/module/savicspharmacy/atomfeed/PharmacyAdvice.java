package org.openmrs.module.savicspharmacy.atomfeed;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;

public class PharmacyAdvice implements AfterReturningAdvice {
	
	private static final String SAVE_METHOD = "save";
	
	private static final String DELETE_METHOD = "delete";
	
	private static final int FIRST_TRANSACTION_MANAGER_INDEX = 0;
	
	private static final String UUID_PATTERN_TO_REPLACE = "{uuid}";
	
	private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
	
	private EventService eventService;
	
	public PharmacyAdvice() {
		atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
		AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
		eventService = new EventServiceImpl(allEventRecordsQueue);
	}
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] parameters, Object o1) {
		String execMethodName = method.getName();
		if (!SAVE_METHOD.equals(execMethodName) && !DELETE_METHOD.equals(execMethodName)) {
			return;
		}
	}
	
	private PlatformTransactionManager getSpringPlatformTransactionManager() {
		return Context.getRegisteredComponents(PlatformTransactionManager.class).get(FIRST_TRANSACTION_MANAGER_INDEX);
	}
	
}
