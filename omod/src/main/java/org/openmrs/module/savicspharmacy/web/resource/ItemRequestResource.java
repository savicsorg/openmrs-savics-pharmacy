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
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.Route;
import org.openmrs.module.savicspharmacy.api.entity.Unit;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/item", supportedClass = Item.class, supportedOpenmrsVersions = { "2.*.*" })
public class ItemRequestResource extends DataDelegatingCrudResource<Item> {
	
	@Override
	public Item newDelegate() {
		return new Item();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("description");
			description.addProperty("buyPrice");
			description.addProperty("sellPrice");
			description.addProperty("virtualstock");
			description.addProperty("soh");
			description.addProperty("stockMin");
			description.addProperty("stockMax");
			description.addProperty("AMC");
			description.addProperty("unit");
			description.addProperty("route");
			description.addProperty("numberOfExpiredLots");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("description");
			description.addProperty("buyPrice");
			description.addProperty("sellPrice");
			description.addProperty("virtualstock");
			description.addProperty("soh");
			description.addProperty("stockMin");
			description.addProperty("stockMax");
			description.addProperty("AMC");
			description.addProperty("unit");
			description.addProperty("route");
			description.addProperty("numberOfExpiredLots");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("description");
			description.addProperty("buyPrice");
			description.addProperty("sellPrice");
			description.addProperty("virtualstock");
			description.addProperty("soh");
			description.addProperty("stockMin");
			description.addProperty("stockMax");
			description.addProperty("AMC");
			description.addProperty("unit");
			description.addProperty("route");
			description.addProperty("numberOfExpiredLots");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Item> itemList = Context.getService(PharmacyService.class).getAll(Item.class, context.getLimit(),
		    context.getStartIndex());
		return new AlreadyPaged<Item>(context, itemList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Item> itemList = Context.getService(PharmacyService.class).doSearch(Item.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Item>(context, itemList, false);
	}
	
	@Override
	public Item getByUniqueId(String uuid) {
		
		return (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class, uuid);
	}
	
	@Override
	public Item save(Item item) {
		return (Item) Context.getService(PharmacyService.class).upsert(item);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		Item item = this.constructItem(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(item);
		return ConversionUtil.convertToRepresentation(item, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Item item = this.constructItem(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(item);
		return ConversionUtil.convertToRepresentation(item, context.getRepresentation());
	}
	
	@Override
	protected void delete(Item item, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(item);
	}
	
	@Override
	public void purge(Item item, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(item);
	}
	
	private Item constructItem(String uuid, SimpleObject properties) {
		Item item;
		Unit unit = null;
		if (properties.get("unit") != null) {
			Integer unitId = properties.get("unit");
			unit = (Unit) Context.getService(PharmacyService.class).getEntityByid(Unit.class, "id", unitId);
		}
		Route route = null;
		if (properties.get("route") != null) {
			Integer routeId = properties.get("route");
			route = (Route) Context.getService(PharmacyService.class).getEntityByid(Route.class, "id", routeId);
		}
		if (uuid != null) {
			item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class, uuid);
			if (item == null) {
				throw new IllegalPropertyException("item not exist");
			}
			
			if (properties.get("name") != null) {
				item.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				item.setCode((String) properties.get("code"));
			}
			
			if (properties.get("description") != null) {
				item.setDescription((String) properties.get("description"));
			}
			
			if (properties.get("buyPrice") != null) {
				item.setBuyPrice((Integer) properties.get("buyPrice"));
			}
			
			if (properties.get("sellPrice") != null) {
				item.setSellPrice((Double) properties.get("sellPrice"));
			}
			
			if (properties.get("virtualstock") != null) {
				item.setVirtualstock((Integer) properties.get("virtualstock"));
			}
			
			if (properties.get("soh") != null) {
				item.setSoh((Integer) properties.get("soh"));
			}
			
			if (properties.get("stockMin") != null) {
				item.setStockMin((Integer) properties.get("stockMin"));
			}
			
			if (properties.get("stockMax") != null) {
				item.setStockMax((Integer) properties.get("stockMax"));
			}
			
			if (properties.get("AMC") != null) {
				item.setAMC((Double) properties.get("AMC"));
			}
			
			item.setUnit(unit);
			item.setRoute(route);
		} else {
			item = new Item();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, code");
			}
			System.out.println("------ > " + properties.toString());
			item.setName((String) properties.get("name"));
			item.setCode((String) properties.get("code"));
			item.setDescription((String) properties.get("description"));
			item.setBuyPrice(new Double(properties.get("buyPrice").toString()));
			item.setSellPrice(new Double(properties.get("sellPrice").toString()));
			item.setVirtualstock((Integer) properties.get("virtualstock"));
			item.setSoh(new Integer(properties.get("soh").toString()));
			item.setStockMin(new Integer(properties.get("stockMin").toString()));
			item.setStockMax(new Integer(properties.get("stockMax").toString()));
			item.setAMC((Double) properties.get("AMC"));
			item.setUnit(unit);
			item.setRoute(route);
		}
		
		return item;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/item";
	}
	
}
