/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.SavicsPharmacyModuleActivator;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * @author anatoleabe
 */
public class AutoCloseExternalVisitTask extends AbstractTask {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		String gp = "7";
		if (!isExecuting) {
			if (log.isDebugEnabled()) {
				log.debug("Savics starting Auto Close Visits Task old than fixed days...");
			}
			
			startExecuting();
			try {
				gp = Context.getAdministrationService().getGlobalProperty(
				    SavicsPharmacyModuleActivator.GLOBAL_PROPERTY_TASK_AUTO_CLOSE_VISIT_DELAY);
				
				String openVisitsQuery = "SELECT v.visit_id\n" + "from visit v\n"
				        + "where v.date_started <=  NOW() - INTERVAL " + gp + " DAY and v.date_stopped is null and \n"
				        + "\n" + "v.visit_id not in (\n" + "	\n" + "	SELECT e.`visit_id`\n" + "	FROM encounter e\n"
				        + "	inner join encounter_type et on et.`encounter_type_id`=e.`encounter_type`\n"
				        + "	inner join visit v on v.visit_id = e.visit_id\n" + "	WHERE\n" + "	e.`voided`=0\n"
				        + "	and v.`voided`=0 \n" + "	and et.uuid='181820aa-88c9-479b-9077-af92f5364329'\n"
				        + "	#Visit not in hospitalisation\n" + "	)";
				
				List<List<Object>> results = Context.getAdministrationService().executeSQL(openVisitsQuery, true);
				for (List<Object> temp : results) {
					for (Object value : temp) {
						if (value != null) {
							Visit v = Context.getVisitService().getVisit((Integer) value);
							Context.getVisitService().endVisit(v, new Date());
						}
					}
				}
				
				//                Calendar date = Calendar.getInstance();
				//                date.add(Calendar.DATE, -7);
				//                Context.getVisitService().stopVisits(date.getTime());
			}
			catch (Exception e) {
				log.error("Error while auto closing visits old than" + gp + " days", e);
			}
			finally {
				stopExecuting();
			}
		}
	}
	
}
