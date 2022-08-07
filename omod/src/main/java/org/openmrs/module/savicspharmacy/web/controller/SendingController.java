/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.savicspharmacy.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.entity.DrugItemOrder;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.entity.Sending;
import org.openmrs.module.savicspharmacy.api.entity.SendingDetail;
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.springframework.stereotype.Controller;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author anatoleabe The main controller.
 */
@Controller
public class SendingController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	PharmacyService pharmacyService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/sending/count")
	public void doCount(HttpServletResponse response, HttpServletRequest request) throws IOException {
		Long count = Context.getService(PharmacyService.class).doCount(Sending.class);
		String content = "{\"count\":" + count + "}";
		response.setContentType("application/json");
		response.setContentLength(content.length());
		response.getWriter().write(content);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/activeVisitPrescription")
	public void getActiveVisitPrescription(HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setContentType("application/octet-stream");
		List<String> listOfPrescribedDrugs = new ArrayList<String>();
		if (request.getParameter("visit") != null) {
			
			Visit visit = null;
			if (request.getParameter("visit") != null) {
				String visitId = request.getParameter("visit");
				visit = (Visit) Context.getService(PharmacyService.class).getEntityByUuid(Visit.class, visitId);
			}
			
			if (visit.getVoided() == false) {
				Set<Encounter> visitEncounterList = visit.getEncounters();
				for (Encounter e : visitEncounterList) {
					Set<Obs> obsByEncounterList = e.getObs();
					String itemContent = "{";
					Integer drugQuantity = 0;
					
					Drug selectedDrug = null;
					
					for (Obs o : obsByEncounterList) {
						Drug drug;
						if (o.getValueNumeric() != null && o.getVoided() == false
						        && "160856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".equalsIgnoreCase(o.getConcept().getUuid())) {
							drugQuantity = o.getValueNumeric().intValue();
							if (!"{".equals(itemContent)) {
								itemContent = itemContent + ",\"quantity\":" + drugQuantity;
							} else {
								itemContent = itemContent + "\"quantity\":" + drugQuantity;
							}
							
						}
						
						drug = o.getValueDrug();
						if (drug != null && o.getVoided() == false) {
							selectedDrug = drug;
							if (!"{".equals(itemContent)) {
								itemContent = itemContent + ",";
							}
							SendingDetail sd = new SendingDetail();
							Item item = (Item) Context.getService(PharmacyService.class).getEntityByAttributes(Item.class,
							    new String[] { "drug.drugId" }, new Object[] { drug.getDrugId() });
							
							Set<ItemsLine> itemsLines = item.getItemsLines();
							
							itemContent = itemContent + "\"name\":\"" + item.getName() + "\"," + "\"id\":" + item.getId()
							        + ", \"code\":\"" + item.getCode() + "\",\"uuid\":\"" + item.getUuid()
							        + "\",\"sellPrice\":" + item.getSellPrice() + ",\"encounter\":\"" + e.getId() + "\""
							        + ",\"drug\":\"" + drug.getId() + "\"";
						}
						
					}
					itemContent = itemContent + "}";
					
					if (selectedDrug != null) {// In case the encounter does not contains a drug prescription
						List<DrugItemOrder> drugItemOrders = (List<DrugItemOrder>) Context.getService(PharmacyService.class)
						        .getListByAttributes(DrugItemOrder.class,
						            new String[] { "visit.visitId", "encounter.encounterId", "drug.drugId" },
						            new Object[] { visit.getVisitId(), e.getEncounterId(), selectedDrug.getDrugId() });
						
						if (drugItemOrders == null || (drugItemOrders != null && drugItemOrders.isEmpty())) {//Here we check if this prescription has already been dispensed. drugItemOrders isEmpty if not yet dispensed
							if (!"{}".equals(itemContent)) {
								listOfPrescribedDrugs.add(itemContent);
							}
						}
					}
					
				}
			}
		}
		String result = "{\"results\":" + listOfPrescribedDrugs.toString() + "}";
		response.setContentType("application/json");
		response.setContentLength(listOfPrescribedDrugs.size());
		response.getWriter().write(result);
	}
	
}
