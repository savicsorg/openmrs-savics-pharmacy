package org.openmrs.module.savicspharmacy.rest.v1_0.resource;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + PharmacyRest.PHARMA_NAMESPACE)
public class PharmacyRest extends MainResourceController {
	
	/**
	 * * @see org.openmrs.module.webservices.rest.web.v1_0.controller.
	 * BaseRestController#getNamespace()
	 */
	public static final String PHARMA_NAMESPACE = "/savicspharmacy";
	
	@Override
	public String getNamespace() {
		return RestConstants.VERSION_1 + PHARMA_NAMESPACE;
	}
}
