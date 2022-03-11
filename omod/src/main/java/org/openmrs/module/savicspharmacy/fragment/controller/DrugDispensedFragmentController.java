/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.fragment.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.entity.Sending;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

/**
 * @author anatoleabe
 */
public class DrugDispensedFragmentController {
	
	public void controller(@FragmentParam("patientId") Patient patient,
	        @SpringBean("pharmacyService") PharmacyService pharmacyService,
	        @SpringBean("visitService") VisitService visitService, @SpringBean("encounterService") EncounterService service,
	        FragmentModel model) throws Exception {
		
		List<Sending> sendings;
		
		List<Visit> patientVisits = visitService.getVisitsByPatient(patient);
		Map<Date, List<Sending>> map = new HashMap<Date, List<Sending>>();
		for (Visit v : patientVisits) {
			sendings = Context.getService(PharmacyService.class).getByMasterId(Sending.class, "visit.id", v.getId());
			if (sendings != null && sendings.size() > 0) {
				map.put(v.getStartDatetime(), sendings);
			}
		}
		
		model.addAttribute("dispensedDrugs", map);
		
	}
}
