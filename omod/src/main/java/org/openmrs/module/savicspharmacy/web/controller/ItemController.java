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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.hibernate.criterion.Projections.count;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.export.DrugsExcelExport;
import org.openmrs.module.savicspharmacy.export.ExpiredStockExcelExport;
import org.openmrs.module.savicspharmacy.export.StockAtRiskExcelExport;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author anatoleabe The main controller.
 */
@Controller
public class ItemController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	PharmacyService pharmacyService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/items/export")
	public void exportToExcel(HttpServletResponse response) throws IOException {
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Drugs_List_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		
		List<Item> itemList = pharmacyService.getAll(Item.class);
		
		DrugsExcelExport excelExporter = new DrugsExcelExport(itemList);
		
		excelExporter.export(response);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/items/stockatrisk")
	public void stockAtRiskToExcel(HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Drugs_List_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		
		Boolean atriskOnly = false;
		if (request.getParameter("atriskOnly") != null) {
			atriskOnly = Boolean.valueOf(request.getParameter("atriskOnly"));
		}
		
		List<Item> itemList = pharmacyService.getAll(Item.class);
		
		StockAtRiskExcelExport stockAtRiskExcelExport = new StockAtRiskExcelExport(itemList, atriskOnly);
		
		stockAtRiskExcelExport.export(response);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/items/expiredstock")
	public void expiredStockToExcel(HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setContentType("application/octet-stream");
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String currentDateTime = dateFormatter.format(new Date());
		
		Integer itemValue = Integer.parseInt(request.getParameter("item"));
		Boolean expiredOnly = false;
		
		if (request.getParameter("expired") != null) {
			expiredOnly = Boolean.valueOf(request.getParameter("expired"));
		}
		
		List<ItemsLine> itemLinestList = new ArrayList<ItemsLine>();
		if (request.getParameter("item") != null) {
			itemLinestList = Context.getService(PharmacyService.class).getByMasterId(ItemsLine.class, "item.id", itemValue);
		}
		
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=Drugs_List_" + currentDateTime + ".xlsx";
		response.setHeader(headerKey, headerValue);
		
		ExpiredStockExcelExport expiredStockExcelExport = new ExpiredStockExcelExport(itemLinestList, expiredOnly);
		
		expiredStockExcelExport.export(response);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/items/count")
	public void doCount(HttpServletResponse response, HttpServletRequest request) throws IOException {
		Long count = Context.getService(PharmacyService.class).doCount(Item.class);
		String content = "{\"count\":" + count + "}";
		response.setContentType("application/json");
		response.setContentLength(content.length());
		response.getWriter().write(content);
	}
}
