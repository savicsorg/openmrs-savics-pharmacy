package org.openmrs.module.savicspharmacy.web.resource;

import java.util.Date;
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
import org.openmrs.module.savicspharmacy.api.entity.CustomerType;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.Stocktake;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/stocktake", supportedClass = Stocktake.class, supportedOpenmrsVersions = { "2.*.*" })
public class StocktakeRequestResource extends DataDelegatingCrudResource<Stocktake> {
	
	@Override
	public Stocktake newDelegate() {
		return new Stocktake();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("name");
			description.addProperty("pharmacyLocation");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("name");
			description.addProperty("pharmacyLocation");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("name");
			description.addProperty("pharmacyLocation");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Stocktake> stocktakeList = Context.getService(PharmacyService.class).getAll(Stocktake.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(stocktakeList.toString());
		return new AlreadyPaged<Stocktake>(context, stocktakeList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Stocktake> stocktakeList = Context.getService(PharmacyService.class).doSearch(Stocktake.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Stocktake>(context, stocktakeList, false);
	}
	
	@Override
	public Stocktake getByUniqueId(String uuid) {
		
		return (Stocktake) Context.getService(PharmacyService.class).getEntityByUuid(Stocktake.class, uuid);
	}
	
	@Override
	public Stocktake save(Stocktake stocktake) {
		return (Stocktake) Context.getService(PharmacyService.class).upsert(stocktake);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		System.out.println("-----------------------------");
		System.out.println(propertiesToCreate);
		System.out.println();
		Stocktake stocktake = this.constructStocktake(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(stocktake);
		return ConversionUtil.convertToRepresentation(stocktake, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Stocktake stocktake = this.constructStocktake(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(stocktake);
		return ConversionUtil.convertToRepresentation(stocktake, context.getRepresentation());
	}
	
	@Override
	protected void delete(Stocktake stocktake, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(stocktake);
	}
	
	@Override
	public void purge(Stocktake stocktake, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(stocktake);
	}
	
	private Stocktake constructStocktake(String uuid, SimpleObject properties) {
		Stocktake stocktake;
		
		PharmacyLocation pharmacyLocation = null;
		if (properties.get("pharmacyLocation") != null) {
			Integer pharmacyLocationId = properties.get("pharmacyLocation");
			pharmacyLocation = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByid(
			    PharmacyLocation.class, "id", pharmacyLocationId);
		}
		
		if (uuid != null) {
			stocktake = (Stocktake) Context.getService(PharmacyService.class).getEntityByUuid(Stocktake.class, uuid);
			if (stocktake == null) {
				throw new IllegalPropertyException("stocktake not exist");
			}
			
			if (properties.get("name") != null) {
				stocktake.setName((String) properties.get("name"));
			}
			
			if (properties.get("date") != null) {
				stocktake.setDate((Date) properties.get("date"));
			}
			
			if (properties.get("pharmacyLocation") != null) {
				stocktake.setPharmacyLocation(pharmacyLocation);
			}
			
		} else {
			stocktake = new Stocktake();
			if (properties.get("name") == null || properties.get("date") == null) {
				throw new IllegalPropertyException("Required parameters: name, date");
			}
			stocktake.setName((String) properties.get("name"));
			stocktake.setDate((Date) properties.get("date"));
			stocktake.setPharmacyLocation(pharmacyLocation);
		}
		
		return stocktake;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/stocktake";
	}
	
}
