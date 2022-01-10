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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.entity.Unit;
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
public class UnitController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	PharmacyService pharmacyService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE
	        + "/unit/count")
	public void doCount(HttpServletResponse response, HttpServletRequest request) throws IOException {
		Long count = Context.getService(PharmacyService.class).doCount(Unit.class);
		String content = "{\"count\":" + count + "}";
		response.setContentType("application/json");
		response.setContentLength(content.length());
		response.getWriter().write(content);
	}
	
}
