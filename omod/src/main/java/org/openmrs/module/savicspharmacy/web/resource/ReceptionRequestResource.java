package org.openmrs.module.savicspharmacy.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import org.openmrs.module.savicspharmacy.api.entity.Reception;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/reception", supportedClass = Reception.class, supportedOpenmrsVersions = { "2.*.*" })
public class ReceptionRequestResource extends DelegatingCrudResource<Reception> {
	
	@Override
	public Reception newDelegate() {
		return new Reception();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("person");
			description.addProperty("pharmacyOrder");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("person");
			description.addProperty("pharmacyOrder");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("person");
			description.addProperty("pharmacyOrder");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Reception> agentList = Context.getService(PharmacyService.class).getAll(Reception.class, context.getLimit(),
		    context.getStartIndex());
		return new AlreadyPaged<Reception>(context, agentList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public Reception getByUniqueId(String uuid) {
		
		return (Reception) Context.getService(PharmacyService.class).getEntityByUuid(Reception.class, uuid);
	}
	
	@Override
	public Reception save(Reception reception) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	protected void delete(Reception reception, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public void purge(Reception reception, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(reception);
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/reception";
	}
	
}
