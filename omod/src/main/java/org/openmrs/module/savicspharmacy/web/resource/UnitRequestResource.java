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
import org.openmrs.module.savicspharmacy.api.entity.Unit;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMA_NAMESPACE + "/unit", supportedClass = Unit.class, supportedOpenmrsVersions = { "2.*.*" })
public class UnitRequestResource extends DataDelegatingCrudResource<Unit> {
	
	@Override
	public Unit newDelegate() {
		return new Unit();
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
		System.out.println("---- doGetAll ");
		List<Unit> districtList = Context.getService(PharmacyService.class).getAll(Unit.class, context.getLimit(),
		    context.getStartIndex());
		System.out.println(districtList.toString());
		return new AlreadyPaged<Unit>(context, districtList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Unit> districtList = Context.getService(PharmacyService.class).doSearch(Unit.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Unit>(context, districtList, false);
	}
	
	@Override
	public Unit getByUniqueId(String uuid) {
		
		return (Unit) Context.getService(PharmacyService.class).getEntityByUuid(Unit.class, uuid);
	}
	
	@Override
	public Unit save(Unit unit) {
		return (Unit) Context.getService(PharmacyService.class).upsert(unit);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		System.out.println("-----------------------------");
		System.out.println(propertiesToCreate);
		System.out.println();
		Unit unit = this.constructDistrict(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(unit);
		return ConversionUtil.convertToRepresentation(unit, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Unit unit = this.constructDistrict(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(unit);
		return ConversionUtil.convertToRepresentation(unit, context.getRepresentation());
	}
	
	@Override
	protected void delete(Unit unit, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(unit);
	}
	
	@Override
	public void purge(Unit unit, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(unit);
	}
	
	private Unit constructDistrict(String uuid, SimpleObject properties) {
		Unit unit;
		
		if (uuid != null) {
			unit = (Unit) Context.getService(PharmacyService.class).getEntityByUuid(Unit.class, uuid);
			if (unit == null) {
				throw new IllegalPropertyException("unit not exist");
			}
			
			if (properties.get("name") != null) {
				unit.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				unit.setCode((String) properties.get("code"));
			}
			
		} else {
			unit = new Unit();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, name");
			}
			unit.setName((String) properties.get("name"));
			unit.setCode((String) properties.get("code"));
		}
		
		return unit;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/unit";
	}
	
}
