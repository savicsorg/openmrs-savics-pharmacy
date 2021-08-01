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
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/itemsLine", supportedClass = ItemsLine.class, supportedOpenmrsVersions = { "2.*.*" })
public class ItemsLineRequestResource extends DelegatingCrudResource<ItemsLine> {
	
	@Override
	public ItemsLine newDelegate() {
		return new ItemsLine();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			System.out.println("");
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("itemBatch");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemVirtualstock");
			description.addProperty("itemSoh");
			description.addProperty("item");
			description.addProperty("pharmacyLocation");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("itemBatch");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemVirtualstock");
			description.addProperty("itemSoh");
			description.addProperty("item");
			description.addProperty("pharmacyLocation");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("itemBatch");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemVirtualstock");
			description.addProperty("itemSoh");
			description.addProperty("item");
			description.addProperty("pharmacyLocation");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ItemsLine> itemsLineList = Context.getService(PharmacyService.class).getAll(ItemsLine.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<ItemsLine>(context, itemsLineList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("itemBatch");
		Integer itemValue = Integer.parseInt(context.getParameter("item"));
		List<ItemsLine> itemLinestList;
		if (itemValue != null) {
			itemLinestList = Context.getService(PharmacyService.class).getByMasterId(ItemsLine.class, "item.id", itemValue,
			    context.getLimit(), context.getStartIndex());
		} else {
			itemLinestList = Context.getService(PharmacyService.class).doSearch(ItemsLine.class, "itemBatch", value,
			    context.getLimit(), context.getStartIndex());
		}
		
		return new AlreadyPaged<ItemsLine>(context, itemLinestList, false);
	}
	
	@Override
	public ItemsLine getByUniqueId(String uuid) {
		
		return (ItemsLine) Context.getService(PharmacyService.class).getEntityByUuid(ItemsLine.class, uuid);
	}
	
	@Override
	public ItemsLine save(ItemsLine itemsLine) {
		return (ItemsLine) Context.getService(PharmacyService.class).upsert(itemsLine);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("itemBatch") == null) {
			throw new ConversionException("Required properties: itemBatch");
		}
		
		ItemsLine itemsLine;
		try {
			itemsLine = this.constructAgent(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(itemsLine);
			return ConversionUtil.convertToRepresentation(itemsLine, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(ItemsLineRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		ItemsLine itemsLine;
		try {
			itemsLine = this.constructAgent(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(itemsLine);
			return ConversionUtil.convertToRepresentation(itemsLine, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(ItemsLineRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	protected void delete(ItemsLine itemsLine, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(itemsLine);
	}
	
	@Override
	public void purge(ItemsLine itemsLine, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(itemsLine);
	}
	
	private ItemsLine constructAgent(String uuid, SimpleObject properties) throws ParseException {
		ItemsLine itemsLine;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = (Integer) properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
			System.out.println("item = " + itemId + " item object = " + item);
		}
		
		PharmacyLocation pharmacyLocation = null;
		if (properties.get("pharmacyLocation") != null) {
			
			Integer pharmacyLocationId = (Integer) properties.get("pharmacyLocation");
			
			pharmacyLocation = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByid(
			    PharmacyLocation.class, "id", pharmacyLocationId);
			System.out.println("pharmacyLocationId = " + pharmacyLocationId + " location object = " + pharmacyLocation);
		}
		
		if (uuid != null) {
			itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByUuid(ItemsLine.class, uuid);
			if (itemsLine == null) {
				throw new IllegalPropertyException("Items line not exist");
			}
			
			if (properties.get("itemBatch") != null) {
				itemsLine.setItemBatch((String) properties.get("itemBatch"));
			}
			
			if (properties.get("itemExpiryDate") != null) {
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			}
			
			if (properties.get("itemVirtualstock") != null) {
				itemsLine.setItemVirtualstock((Integer) properties.get("itemVirtualstock"));
			}
			
			if (properties.get("itemSoh") != null) {
				itemsLine.setItemSoh((Integer) properties.get("itemSoh"));
			}
			
			if (properties.get("item") != null) {
				itemsLine.setItem(item);
			}
			
			if (properties.get("pharmacyLocation") != null) {
				itemsLine.setPharmacyLocation(pharmacyLocation);
			}
			
		} else {
			itemsLine = new ItemsLine();
			
			if (properties.get("itemBatch") == null) {
				throw new ConversionException("Required properties: itemBatch");
			}
			itemsLine.setItemBatch((String) properties.get("itemBatch"));
			itemsLine.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			itemsLine.setItemVirtualstock((Integer) properties.get("itemVirtualstock"));
			itemsLine.setItemSoh((Integer) properties.get("itemSoh"));
			itemsLine.setItem(item);
			itemsLine.setPharmacyLocation(pharmacyLocation);
		}
		
		return itemsLine;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/itemsLine";
	}
	
}
