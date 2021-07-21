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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.api.entity.Route;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.web.serialization.ObjectMapperRepository;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author anatoleabe
 * The main controller.
 */
@Controller
public class RouteController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	UserService userService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/route/all")
	@ResponseBody
	public String getAllRoutes() throws IOException {
		ObjectMapperRepository objectMapperRepository = new ObjectMapperRepository();
		PharmacyService gmaoService = Context.getService(PharmacyService.class);
		return objectMapperRepository.writeValueAsString(gmaoService.getAll(Route.class));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/route/test")
	@ResponseBody
	public String testit() throws IOException {
		ObjectMapperRepository objectMapperRepository = new ObjectMapperRepository();
		PharmacyService gmaoService = Context.getService(PharmacyService.class);
		List<Route> list1 = gmaoService.getAll(Route.class);
		return objectMapperRepository.writeValueAsString(list1);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/rest/" + RestConstants.VERSION_1
	        + PharmacyRest.PHARMACY_NAMESPACE + "/route/test")
	@ResponseBody
	public String upsert() throws IOException {
		ObjectMapperRepository objectMapperRepository = new ObjectMapperRepository();
		PharmacyService gmaoService = Context.getService(PharmacyService.class);
		List<Route> list1 = gmaoService.getAll(Route.class);
		return objectMapperRepository.writeValueAsString(list1);
	}
}
