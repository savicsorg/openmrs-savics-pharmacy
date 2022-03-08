/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.savicspharmacy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class SavicsPharmacyModuleActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private AdministrationService administrationService;
	
	public static final String GLOBAL_PROPERTY_PHARMACY_EXPIRED_DELAY = "savics.pharmacy.batches.expiration.prevision.delay";
	
	public static final String GLOBAL_PROPERTY_TASK_AUTO_CLOSE_VISIT_DELAY = "savics.pharmacy.tasks.autoCloseExternalVisitAfter";
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started Savics Pharmacy Module");
		administrationService = Context.getAdministrationService();
		GlobalProperty gp;
		
		String property = administrationService.getGlobalProperty(GLOBAL_PROPERTY_PHARMACY_EXPIRED_DELAY);
		if (property == null || property.isEmpty()) {
			gp = new GlobalProperty(GLOBAL_PROPERTY_PHARMACY_EXPIRED_DELAY, "30");
			gp.setDescription("Savics Pharmacy expiration prevision delay (in days). Default 30 days");
			administrationService.saveGlobalProperty(gp);
		}
		
		String property2 = administrationService.getGlobalProperty(GLOBAL_PROPERTY_TASK_AUTO_CLOSE_VISIT_DELAY);
		if (property2 == null || property2.isEmpty()) {
			gp = new GlobalProperty(GLOBAL_PROPERTY_TASK_AUTO_CLOSE_VISIT_DELAY, "7");
			gp.setDescription("Savics task: number of days after which external visit will automacically be closed. Default 7 days");
			administrationService.saveGlobalProperty(gp);
		}
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown Savics Pharmacy Module");
	}
	
}
