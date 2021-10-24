package org.openmrs.module.savicspharmacy.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/location", supportedClass = PharmacyLocation.class, supportedOpenmrsVersions = { "2.*.*" })
public class PharmacyLocationRequestResource extends DataDelegatingCrudResource<PharmacyLocation> {
	
	@Override
	public PharmacyLocation newDelegate() {
		return new PharmacyLocation();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<PharmacyLocation> districtList = Context.getService(PharmacyService.class).getAll(PharmacyLocation.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(districtList.toString());
		return new AlreadyPaged<PharmacyLocation>(context, districtList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<PharmacyLocation> districtList = Context.getService(PharmacyService.class).doSearch(PharmacyLocation.class,
		    "name", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<PharmacyLocation>(context, districtList, false);
	}
	
	@Override
	public PharmacyLocation getByUniqueId(String uuid) {
		
		return (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByUuid(PharmacyLocation.class, uuid);
	}
	
	@Override
	public PharmacyLocation save(PharmacyLocation location) {
		return (PharmacyLocation) Context.getService(PharmacyService.class).upsert(location);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		PharmacyLocation location = this.constructDistrict(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(location);
		return ConversionUtil.convertToRepresentation(location, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		PharmacyLocation location = this.constructDistrict(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(location);
		return ConversionUtil.convertToRepresentation(location, context.getRepresentation());
	}
	
	@Override
	protected void delete(PharmacyLocation location, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(location);
	}
	
	@Override
	public void purge(PharmacyLocation location, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(location);
	}
	
	private PharmacyLocation constructDistrict(String uuid, SimpleObject properties) {
		PharmacyLocation location;
		
		if (uuid != null) {
			location = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByUuid(PharmacyLocation.class,
			    uuid);
			if (location == null) {
				throw new IllegalPropertyException("location not exist");
			}
			
			if (properties.get("name") != null) {
				location.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				location.setCode((String) properties.get("code"));
			}
			
		} else {
			location = new PharmacyLocation();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, code");
			}
			location.setName((String) properties.get("name"));
			location.setCode((String) properties.get("code"));
		}
		
		return location;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/location";
	}
	
}
