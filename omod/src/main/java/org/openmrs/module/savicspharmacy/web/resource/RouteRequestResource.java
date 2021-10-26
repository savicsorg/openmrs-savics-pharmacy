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
import org.openmrs.module.savicspharmacy.api.entity.Route;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/route", supportedClass = Route.class, supportedOpenmrsVersions = { "2.*.*" })
public class RouteRequestResource extends DataDelegatingCrudResource<Route> {
	
	@Override
	public Route newDelegate() {
		return new Route();
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
		List<Route> routeList = Context.getService(PharmacyService.class).getAll(Route.class, context.getLimit(),
		    context.getStartIndex());
		System.out.println(routeList.toString());
		return new AlreadyPaged<Route>(context, routeList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Route> routeList = Context.getService(PharmacyService.class).doSearch(Route.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Route>(context, routeList, false);
	}
	
	@Override
	public Route getByUniqueId(String uuid) {
		
		return (Route) Context.getService(PharmacyService.class).getEntityByUuid(Route.class, uuid);
	}
	
	@Override
	public Route save(Route route) {
		return (Route) Context.getService(PharmacyService.class).upsert(route);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		Route route = this.constructRoute(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(route);
		return ConversionUtil.convertToRepresentation(route, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Route route = this.constructRoute(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(route);
		return ConversionUtil.convertToRepresentation(route, context.getRepresentation());
	}
	
	@Override
	protected void delete(Route route, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(route);
	}
	
	@Override
	public void purge(Route route, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(route);
	}
	
	private Route constructRoute(String uuid, SimpleObject properties) {
		Route route;
		
		if (uuid != null) {
			route = (Route) Context.getService(PharmacyService.class).getEntityByUuid(Route.class, uuid);
			if (route == null) {
				throw new IllegalPropertyException("route not exist");
			}
			
			if (properties.get("name") != null) {
				route.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				route.setCode((String) properties.get("code"));
			}
			
		} else {
			route = new Route();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, name");
			}
			route.setName((String) properties.get("name"));
			route.setCode((String) properties.get("code"));
		}
		
		return route;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/route";
	}
	
}
