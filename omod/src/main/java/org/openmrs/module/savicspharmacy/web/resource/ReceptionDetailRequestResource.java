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
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.entity.ReceptionDetail;
import org.openmrs.module.savicspharmacy.api.entity.ReceptionDetailId;
import org.openmrs.module.savicspharmacy.api.entity.Reception;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/receptionDetail", supportedClass = ReceptionDetail.class, supportedOpenmrsVersions = { "2.*.*" })
public class ReceptionDetailRequestResource extends DelegatingCrudResource<ReceptionDetail> {
	
	@Override
	public ReceptionDetail newDelegate() {
		return new ReceptionDetail();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("reception");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemBatch");
			description.addProperty("quantityReceived");
			description.addProperty("uuid");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("reception");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemBatch");
			description.addProperty("quantityReceived");
			description.addProperty("uuid");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("reception");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemExpiryDate");
			description.addProperty("itemBatch");
			description.addProperty("quantityReceived");
			description.addProperty("uuid");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<ReceptionDetail> receptionDetailList = Context.getService(PharmacyService.class).getAll(ReceptionDetail.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<ReceptionDetail>(context, receptionDetailList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Integer value = Integer.parseInt(context.getParameter("receptionId"));
		List<ReceptionDetail> receptionDetailList = Context.getService(PharmacyService.class).getByMasterId(
		    ReceptionDetail.class, "reception.id", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<ReceptionDetail>(context, receptionDetailList, false);
	}
	
	@Override
	public ReceptionDetail getByUniqueId(String uuid) {
		return (ReceptionDetail) Context.getService(PharmacyService.class).getEntityByUuid(ReceptionDetail.class, uuid);
	}
	
	@Override
	public ReceptionDetail save(ReceptionDetail receptionDetail) {
		return (ReceptionDetail) Context.getService(PharmacyService.class).upsert(receptionDetail);
		
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		try {
			if (propertiesToCreate.get("quantityReceived") == null) {
				throw new ConversionException("Required properties: quantityReceived");
			}
			
			ReceptionDetail receptionDetail = this.constructReceptionDetail(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(receptionDetail);
			return ConversionUtil.convertToRepresentation(receptionDetail, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(ReceptionDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		try {
			ReceptionDetail receptionDetail = this.constructReceptionDetail(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(receptionDetail);
			
			return ConversionUtil.convertToRepresentation(receptionDetail, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(ReceptionDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	protected void delete(ReceptionDetail receptionDetail, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(receptionDetail);
	}
	
	@Override
	public void purge(ReceptionDetail receptionDetail, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(receptionDetail);
	}
	
	private ReceptionDetail constructReceptionDetail(String uuid, SimpleObject properties) throws ParseException {
		ReceptionDetail receptionDetail;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
		}
		
		Reception reception = null;
		if (properties.get("reception") != null) {
			Integer receptionId = properties.get("reception");
			reception = (Reception) Context.getService(PharmacyService.class).getEntityByid(Reception.class, "id",
			    receptionId);
		}
		if (properties.get("itemBatch") != null && item != null) {
                    ItemsLine itemLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
                        ItemsLine.class, new String[] { "itemBatch", "item.id" },
                        new Object[] { properties.get("itemBatch").toString(), item.getId() });
                    if (itemLine == null)
                            itemLine = new ItemsLine();
                    itemLine.setItem(item);
                    itemLine.setItemBatch(properties.get("itemBatch").toString());
                    itemLine.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
                    itemLine.setItemVirtualstock(Integer.valueOf(properties.get("itemVirtualstock").toString()));
                    PharmacyLocation location = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByUuid(
                        PharmacyLocation.class, properties.get("location").toString());
                    itemLine.setPharmacyLocation(location);
                    Context.getService(PharmacyService.class).upsert(itemLine);		
		}
		
		if (uuid != null) {
			receptionDetail = (ReceptionDetail) Context.getService(PharmacyService.class).getEntityByUuid(
			    ReceptionDetail.class, uuid);
			
			if (receptionDetail == null) {
				throw new IllegalPropertyException("ReceptionDetail not exist");
			}
			
			if (properties.get("orderLineQuantity") != null) {
				receptionDetail.setOrderLineQuantity(Integer.valueOf(properties.get("orderLineQuantity").toString()));
			}
			
			if (properties.get("quantityReceived") != null) {
				receptionDetail.setQuantityReceived(Integer.valueOf(properties.get("quantityReceived").toString()));
			}
			
			if (properties.get("itemBatch") != null) {
				receptionDetail.setItemBatch(properties.get("itemBatch").toString());
			}
			
			if (properties.get("itemExpiryDate") != null) {
				receptionDetail.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			}
			
		} else {
			receptionDetail = new ReceptionDetail();
			if (properties.get("orderLineQuantity") == null) {
				throw new IllegalPropertyException("Required parameters: orderLineQuantity");
			}
			receptionDetail.setOrderLineQuantity(Integer.valueOf(properties.get("orderLineQuantity").toString()));
			receptionDetail.setQuantityReceived(Integer.valueOf(properties.get("quantityReceived").toString()));
			receptionDetail.setItemBatch(properties.get("itemBatch").toString());
			receptionDetail.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			ReceptionDetailId pk = new ReceptionDetailId(item.getId(), reception.getId());
			receptionDetail.setId(pk.hashCode());
			receptionDetail.setPk(pk);
			receptionDetail.setItem(item);
			receptionDetail.setReception(reception);
		}
		
		return receptionDetail;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/receptionDetail";
	}
	
}
