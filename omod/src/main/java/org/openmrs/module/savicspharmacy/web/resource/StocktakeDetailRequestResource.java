package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.module.savicspharmacy.api.entity.CustomerType;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.entity.Stocktake;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.StocktakeDetail;
import org.openmrs.module.savicspharmacy.api.entity.StocktakeDetailId;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/stocktakeDetail", supportedClass = StocktakeDetail.class, supportedOpenmrsVersions = { "2.*.*" })
public class StocktakeDetailRequestResource extends DataDelegatingCrudResource<StocktakeDetail> {
	
	@Override
	public StocktakeDetail newDelegate() {
		return new StocktakeDetail();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("stocktake");
			description.addProperty("itemStock");
			description.addProperty("item");
			description.addProperty("itemPhysicalStock");
			description.addProperty("stocktakeLineComments");
			description.addProperty("adjustQuantity");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("stocktake");
			description.addProperty("itemStock");
			description.addProperty("item");
			description.addProperty("itemPhysicalStock");
			description.addProperty("stocktakeLineComments");
			description.addProperty("adjustQuantity");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("stocktake");
			description.addProperty("itemStock");
			description.addProperty("item");
			description.addProperty("itemPhysicalStock");
			description.addProperty("stocktakeLineComments");
			description.addProperty("adjustQuantity");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<StocktakeDetail> stocktakeList = Context.getService(PharmacyService.class).getAll(StocktakeDetail.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(stocktakeList.toString());
		return new AlreadyPaged<StocktakeDetail>(context, stocktakeList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		int value = Integer.valueOf(context.getParameter("stocktakeId"));
		List<StocktakeDetail> stocktakeList = Context.getService(PharmacyService.class).getByMasterId(StocktakeDetail.class,
		    "stocktake.id", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<StocktakeDetail>(context, stocktakeList, false);
	}
	
	@Override
	public StocktakeDetail getByUniqueId(String uuid) {
		
		return (StocktakeDetail) Context.getService(PharmacyService.class).getEntityByUuid(StocktakeDetail.class, uuid);
	}
	
	@Override
	public StocktakeDetail save(StocktakeDetail stocktake) {
		return (StocktakeDetail) Context.getService(PharmacyService.class).upsert(stocktake);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("adjustQuantity") == null) {
			throw new ConversionException("Required properties: adjustQuantity");
		}
		
		StocktakeDetail stocktake;
		try {
			stocktake = this.constructStocktake(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(stocktake);
			return ConversionUtil.convertToRepresentation(stocktake, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(StocktakeDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		StocktakeDetail stocktake;
		try {
			stocktake = this.constructStocktake(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(stocktake);
			return ConversionUtil.convertToRepresentation(stocktake, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(StocktakeDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	protected void delete(StocktakeDetail stocktake, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(stocktake);
	}
	
	@Override
	public void purge(StocktakeDetail stocktake, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(stocktake);
	}
	
	private StocktakeDetail constructStocktake(String uuid, SimpleObject properties) throws ParseException {
		StocktakeDetail detail;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Stocktake stocktake = null;
		if (properties.get("stocktake") != null) {
			Integer stocktakeId = properties.get("stocktake");
			stocktake = (Stocktake) Context.getService(PharmacyService.class).getEntityByid(Stocktake.class, "id",
			    stocktakeId);
		}
		
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Stocktake.class, "id", itemId);
		}
		
		if (uuid != null) {
			detail = (StocktakeDetail) Context.getService(PharmacyService.class)
			        .getEntityByUuid(StocktakeDetail.class, uuid);
			if (detail == null) {
				throw new IllegalPropertyException("detail not exist");
			}
			
			if (properties.get("itemStock") != null) {
				detail.setItemStock(Integer.valueOf(properties.get("itemStock").toString()));
			}
			
			if (properties.get("itemPhysicalStock") != null) {
				detail.setItemPhysicalStock(Integer.valueOf(properties.get("itemPhysicalStock").toString()));
			}
			
			if (properties.get("stocktakeLineComments") != null) {
				detail.setStocktakeLineComments(properties.get("stocktakeLineComments").toString());
			}
			
			if (properties.get("adjustQuantity") != null) {
				detail.setItemPhysicalStock(Integer.valueOf(properties.get("adjustQuantity").toString()));
			}
			
		} else {
			detail = new StocktakeDetail();
			if (properties.get("name") == null || properties.get("date") == null) {
				throw new IllegalPropertyException("Required parameters: name, date");
			}
			if (properties.get("itemStock") != null) {
				detail.setItemStock(Integer.valueOf(properties.get("itemStock").toString()));
			}
			
			if (properties.get("itemPhysicalStock") != null) {
				detail.setItemPhysicalStock(Integer.valueOf(properties.get("itemPhysicalStock").toString()));
			}
			
			if (properties.get("stocktakeLineComments") != null) {
				detail.setStocktakeLineComments(properties.get("stocktakeLineComments").toString());
			}
			
			if (properties.get("adjustQuantity") != null) {
				detail.setItemPhysicalStock(Integer.valueOf(properties.get("adjustQuantity").toString()));
			}
			if (properties.get("stocktake") != null) {
				detail.setStocktake(stocktake);
			}
			
			if (properties.get("item") != null) {
				detail.setItem(item);
			}
			StocktakeDetailId pk = new StocktakeDetailId(item.getId(), stocktake.getId());
			detail.setId(pk.hashCode());
			detail.setPk(pk);
		}
		return detail;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/stocktakeDetail";
	}
	
}
