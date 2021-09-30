package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyOrder;
import org.openmrs.module.savicspharmacy.api.entity.Reception;
import org.openmrs.module.savicspharmacy.api.entity.ReceptionDetail;
import org.openmrs.module.savicspharmacy.api.entity.ReceptionDetailId;
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;

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
			description.addProperty("receptionDetails");
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
			description.addProperty("receptionDetails");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Reception> receptionList = Context.getService(PharmacyService.class).getAll(Reception.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Reception>(context, receptionList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("id");
		List<Reception> receptionList = Context.getService(PharmacyService.class).doSearch(Reception.class, "id", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Reception>(context, receptionList, false);
	}
	
	@Override
	public Reception getByUniqueId(String uuid) {
		
		return (Reception) Context.getService(PharmacyService.class).getEntityByUuid(Reception.class, uuid);
	}
	
	@Override
	public Reception save(Reception reception) {
		return (Reception) Context.getService(PharmacyService.class).upsert(reception);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		try {
			Reception reception = this.constructOrder(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(reception);
			
			reception = (Reception) Context.getService(PharmacyService.class).getEntityByid(Reception.class, "id",
			    reception.getId());
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(reception.getReceptionDetails());
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Transaction transaction;
			for (int i = 0; i < list.size(); i++) {
				ReceptionDetail o = new ReceptionDetail();
				if (o.getOrderLineQuantity() != null)
					o.setOrderLineQuantity(new Integer(list.get(i).get("orderLineQuantity").toString()));
				o.setQuantityReceived(new Integer(list.get(i).get("quantityReceived").toString()));
				o.setItemBatch(list.get(i).get("itemBatch").toString());
				o.setItemExpiryDate(simpleDateFormat.parse(list.get(i).get("itemExpiryDate").toString()));
				
				Integer itemId = new Integer(list.get(i).get("item").toString());
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				
				ItemsLine itemLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, new String[] { "itemBatch", "item.id" },
				    new Object[] { o.getItemBatch(), item.getId() });
				o.setItem(item);
				o.setReception(reception);
				// creation of a new item line
				if (itemLine == null) {
					itemLine = new ItemsLine();
					itemLine.setItem(item);
					itemLine.setItemBatch(list.get(i).get("itemBatch").toString());
					itemLine.setItemExpiryDate(simpleDateFormat.parse(list.get(i).get("itemExpiryDate").toString()));
					itemLine.setItemVirtualstock(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					itemLine.setItemSoh(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					item.setVirtualstock(item.getVirtualstock() + o.getQuantityReceived());
					item.setSoh(item.getSoh() + o.getQuantityReceived());
				} else {
					itemLine.setItemVirtualstock(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					itemLine.setItemSoh(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					item.setVirtualstock(item.getVirtualstock() + o.getQuantityReceived());
					item.setSoh(item.getSoh() + o.getQuantityReceived());
				}
				
				PharmacyLocation location = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByUuid(
				    PharmacyLocation.class, list.get(i).get("itemLineLocation").toString());
				itemLine.setPharmacyLocation(location);
				Context.getService(PharmacyService.class).upsert(itemLine);
				
				ReceptionDetailId receptionDetailId = new ReceptionDetailId(itemId, reception.getId());
				o.setId(0);
				o.setPk(receptionDetailId);
				Context.getService(PharmacyService.class).upsert(o);
				
				//Create a transaction for this operation
				transaction = new Transaction();
				transaction.setDate(new Date());
				transaction.setQuantity(o.getQuantityReceived());
				transaction.setReceptionId(reception.getId());
				transaction.setItem(item);
				transaction.setPharmacyLocation(itemLine.getPharmacyLocation());
				transaction.setItemBatch(itemLine.getItemBatch());
				transaction.setItemExpiryDate(simpleDateFormat.parse(itemLine.getItemExpiryDate().toString()));
				//TODO
				transaction.setPersonId(Context.getUserContext().getAuthenticatedUser().getPerson().getPersonId());
				transaction.setStatus("VALIDATED");
				int transactionType = 6; //rece
				transaction.setTransactionType(transactionType);//disp
				//Upsert the transaction
				Context.getService(PharmacyService.class).upsert(transaction);
			}
			
			return ConversionUtil.convertToRepresentation(reception, context.getRepresentation());
		}
		catch (ParseException e) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Reception reception;
		try {
			reception = this.constructOrder(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(reception);
			
			reception = (Reception) Context.getService(PharmacyService.class).getEntityByid(Reception.class, "id",
			    reception.getId());
			
			//1. delete all ReceptionDetail
			List<ReceptionDetail> detailList = Context.getService(PharmacyService.class).getByMasterId(
			    ReceptionDetail.class, "reception.id", reception.getId(), 1000, 0);
			for (int i = 0; i < detailList.size(); i++) {
				ReceptionDetail o = detailList.get(i);
				Item it = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id",
				    o.getItem().getId());
				ItemsLine line = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, new String[] { "itemBatch", "item.id" }, new Object[] { o.getItemBatch(), it.getId() });
				line.setItemVirtualstock(line.getItemVirtualstock() - o.getQuantityReceived());
				line.setItemSoh(line.getItemSoh() - o.getQuantityReceived());
				it.setVirtualstock(it.getVirtualstock() - o.getQuantityReceived());
				it.setSoh(it.getSoh() - o.getQuantityReceived());
				Context.getService(PharmacyService.class).upsert(line);
				Context.getService(PharmacyService.class).delete(o);
			}
			
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(reception.getReceptionDetails());
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Transaction transaction;
			for (int i = 0; i < list.size(); i++) {
				ReceptionDetail o = new ReceptionDetail();
				if (o.getOrderLineQuantity() != null)
					o.setOrderLineQuantity(new Integer(list.get(i).get("orderLineQuantity").toString()));
				o.setQuantityReceived(new Integer(list.get(i).get("quantityReceived").toString()));
				o.setItemBatch(list.get(i).get("itemBatch").toString());
				o.setItemExpiryDate(simpleDateFormat.parse(list.get(i).get("itemExpiryDate").toString()));
				
				Integer itemId = new Integer(list.get(i).get("item").toString());
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				
				ItemsLine itemLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, new String[] { "itemBatch", "item.id" },
				    new Object[] { o.getItemBatch(), item.getId() });
				o.setItem(item);
				o.setReception(reception);
				// creation of a new item line
				if (itemLine == null) {
					itemLine = new ItemsLine();
					itemLine.setItem(item);
					itemLine.setItemBatch(list.get(i).get("itemBatch").toString());
					itemLine.setItemExpiryDate(simpleDateFormat.parse(list.get(i).get("itemExpiryDate").toString()));
					itemLine.setItemVirtualstock(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					itemLine.setItemSoh(Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					item.setVirtualstock(item.getVirtualstock() + o.getQuantityReceived());
					item.setSoh(item.getSoh() + o.getQuantityReceived());
					
				} else {
					itemLine.setItemVirtualstock(itemLine.getItemVirtualstock()
					        + Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					itemLine.setItemSoh(itemLine.getItemSoh()
					        + Integer.valueOf(list.get(i).get("quantityReceived").toString()));
					item.setVirtualstock(item.getVirtualstock() + o.getQuantityReceived());
					item.setSoh(item.getSoh() + o.getQuantityReceived());
				}
				PharmacyLocation location = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByUuid(
				    PharmacyLocation.class, list.get(i).get("itemLineLocation").toString());
				itemLine.setPharmacyLocation(location);
				Context.getService(PharmacyService.class).upsert(itemLine);
				
				ReceptionDetailId receptionDetailId = new ReceptionDetailId(itemId, reception.getId());
				o.setId(0);
				o.setPk(receptionDetailId);
				Context.getService(PharmacyService.class).upsert(o);
				
				//Create a transaction for this operation
				transaction = new Transaction();
				transaction.setDate(new Date());
				transaction.setQuantity(o.getQuantityReceived());
				transaction.setReceptionId(reception.getId());
				transaction.setItem(item);
				transaction.setPharmacyLocation(itemLine.getPharmacyLocation());
				transaction.setItemBatch(itemLine.getItemBatch());
				transaction.setItemExpiryDate(simpleDateFormat.parse(itemLine.getItemExpiryDate().toString()));
				//TODO
				transaction.setPersonId(Context.getUserContext().getAuthenticatedUser().getPerson().getPersonId());
				transaction.setStatus("VALIDATED");
				int transactionType = 6; //rece
				transaction.setTransactionType(transactionType);//disp
				//Upsert the transaction
				Context.getService(PharmacyService.class).upsert(transaction);
			}
			
			return ConversionUtil.convertToRepresentation(reception, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	protected void delete(Reception reception, String reason, RequestContext context) throws ResponseException {
		List<ReceptionDetail> detailList = Context.getService(PharmacyService.class).getByMasterId(ReceptionDetail.class,
		    "reception.id", reception.getId(), 1000, 0);
		for (int i = 0; i < detailList.size(); i++) {
			ReceptionDetail o = detailList.get(i);
			Item it = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", o.getItem().getId());
			ItemsLine line = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(ItemsLine.class,
			    new String[] { "itemBatch", "item.id" }, new Object[] { o.getItemBatch(), it.getId() });
			line.setItemVirtualstock(line.getItemVirtualstock() - o.getQuantityReceived());
			line.setItemSoh(line.getItemSoh() - o.getQuantityReceived());
			it.setVirtualstock(it.getVirtualstock() - o.getQuantityReceived());
			it.setSoh(it.getSoh() - o.getQuantityReceived());
			Context.getService(PharmacyService.class).upsert(line);
			Context.getService(PharmacyService.class).delete(o);
		}
		Context.getService(PharmacyService.class).delete(reception);
	}
	
	@Override
	public void purge(Reception reception, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(reception);
	}
	
	private Reception constructOrder(String uuid, SimpleObject properties) throws ParseException {
		Reception reception;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		PharmacyOrder order = null;
		if (properties.get("pharmacyOrder") != null) {
			Integer orderId = Integer.valueOf(properties.get("pharmacyOrder").toString());
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByid(PharmacyOrder.class, "id",
			    orderId);
		}
		
		if (uuid != null) {
			reception = (Reception) Context.getService(PharmacyService.class).getEntityByUuid(Reception.class, uuid);
			if (reception == null) {
				throw new IllegalPropertyException("Reception not exist");
			}
			if (properties.get("date") != null) {
				reception.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			
			if (properties.get("receptionDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("receptionDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				reception.setReceptionDetails(set);
			}
			
			if (order != null)
				reception.setPharmacyOrder(order);
		} else {
			reception = new Reception();
			reception.setPerson(Context.getUserContext().getAuthenticatedUser().getPerson());
			reception.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			if (properties.get("receptionDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("receptionDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				reception.setReceptionDetails(set);
			}
			if (order != null) {
				reception.setPharmacyOrder(order);
				//order.setDateReception(simpleDateFormat.parse(properties.get("date").toString()));
				//Context.getService(PharmacyService.class).upsert(order);
			}
		}
		return reception;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/reception";
	}
	
}
