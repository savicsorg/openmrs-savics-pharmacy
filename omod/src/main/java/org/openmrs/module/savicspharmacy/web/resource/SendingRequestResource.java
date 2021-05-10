package org.openmrs.module.savicspharmacy.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.Sending;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/sending", supportedClass = Sending.class, supportedOpenmrsVersions = { "2.*.*" })
public class SendingRequestResource extends DataDelegatingCrudResource<Sending> {
	
	@Override
	public Sending newDelegate() {
		return new Sending();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Sending getByUniqueId(String uuid) {
		
		return (Sending) Context.getService(PharmacyService.class).getEntityByUuid(Sending.class, uuid);
	}
	
	@Override
	public Sending save(Sending sending) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	protected void delete(Sending sending, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public void purge(Sending sending, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/sending";
	}
	
}
