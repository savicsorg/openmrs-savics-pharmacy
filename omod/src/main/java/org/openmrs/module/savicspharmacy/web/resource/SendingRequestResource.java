package org.openmrs.module.savicspharmacy.web.resource;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.entity.Customer;
import org.openmrs.module.savicspharmacy.api.entity.CustomerType;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.Sending;
import org.openmrs.module.savicspharmacy.api.entity.SendingDetail;
import org.openmrs.module.savicspharmacy.api.entity.SendingDetailId;
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import static org.terracotta.modules.ehcache.store.TerracottaClusteredInstanceFactory.LOGGER;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/sending", supportedClass = Sending.class, supportedOpenmrsVersions = { "2.*.*" })
public class SendingRequestResource extends DataDelegatingCrudResource<Sending> {
	
	@Override
	public Sending newDelegate() {
		return new Sending();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addProperty("customerType");
			description.addProperty("numberOfBatches");
			description.addProperty("quantity");
			description.addProperty("validationDate");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addProperty("customerType");
			description.addProperty("numberOfBatches");
			description.addProperty("quantity");
			description.addProperty("validationDate");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("sendingAmount");
			description.addProperty("customer");
			description.addProperty("person");
			description.addProperty("customerType");
			description.addProperty("numberOfBatches");
			description.addProperty("quantity");
			description.addProperty("validationDate");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Sending> agentList = Context.getService(PharmacyService.class).getAll(Sending.class, context.getLimit(),
		    context.getStartIndex());
		return new AlreadyPaged<Sending>(context, agentList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("sendingAmount");
		List<Sending> agentList = Context.getService(PharmacyService.class).doSearch(Sending.class, "sendingAmount", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Sending>(context, agentList, false);
	}
	
	@Override
	public Sending getByUniqueId(String uuid) {
		
		return (Sending) Context.getService(PharmacyService.class).getEntityByUuid(Sending.class, uuid);
	}
	
	@Override
	public Sending save(Sending sending) {
		return (Sending) Context.getService(PharmacyService.class).upsert(sending);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("sendingAmount") == null) {
			throw new ConversionException("Required properties: sendingAmount");
		}
		try {
			Sending sending = this.constructOrder(null, propertiesToCreate);
			sending = (Sending) Context.getService(PharmacyService.class).upsert(sending);
			sending = (Sending) Context.getService(PharmacyService.class)
			        .getEntityByid(Sending.class, "id", sending.getId());
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(sending.getSendingDetails());
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Transaction transaction;
			for (int i = 0; i < list.size(); i++) {
				SendingDetail o = new SendingDetail();
				
				o.setSendingDetailsQuantity(new Integer(list.get(i).get("sendingDetailsQuantity").toString()));
				o.setSendingDetailsValue(Double.valueOf(list.get(i).get("sendingDetailsValue").toString()));
				o.setSendingItemBatch(list.get(i).get("sendingItemBatch").toString());
				
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, new String[] { "itemBatch" }, new Object[] { o.getSendingItemBatch() });
				
				o.setSendingItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				Integer itemId = new Integer(list.get(i).get("item").toString());
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				o.setItem(item);
				o.setSending(sending);
				
				SendingDetailId sendingDetailId = new SendingDetailId(itemId, sending.getId());
				o.setId(0);
				o.setPk(sendingDetailId);
				Context.getService(PharmacyService.class).upsert(o);
				
				//Create a transaction for this operation
				transaction = new Transaction();
				transaction.setDate(new Date());
				transaction.setQuantity(o.getSendingDetailsQuantity());
				transaction.setSendingId(sending.getId());
				transaction.setItem(item);
				transaction.setPharmacyLocation(itemsLine.getPharmacyLocation());
				transaction.setItemBatch(itemsLine.getItemBatch());
				transaction.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				//TODO
				//transaction.setPersonId((Integer) properties.get("personId"));
				transaction.setStatus("INIT");
				int transactionType = 5; //disp
				transaction.setTransactionType(transactionType);//disp
				//Upsert the transaction
				Context.getService(PharmacyService.class).upsert(transaction);
				
				//Update the virtual quantities for item and itemsLine
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() - o.getSendingDetailsQuantity());
				item.setVirtualstock(item.getVirtualstock() - o.getSendingDetailsQuantity());
				
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
			}
			
			return ConversionUtil.convertToRepresentation(sending, context.getRepresentation());
		}
		catch (ParseException e) {
			Logger.getLogger(SendingRequestResource.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Sending sending;
		try {
			sending = this.constructOrder(uuid, propertiesToUpdate);
			if (sending.getValidationDate() != null) {
				sending.setValidationDate(new Date());
			}
			
			Context.getService(PharmacyService.class).upsert(sending);
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			//1. delete all SendingDetail
			List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getByMasterId(
			    SendingDetail.class, "sending.id", sending.getId(), 1000, 0);
			for (int i = 0; i < sendingDetailList.size(); i++) {
				SendingDetail o = sendingDetailList.get(i);
				Context.getService(PharmacyService.class).delete(o);
			}
			
			//2. update all old transactions as canceled
			List<Transaction> transactionlList = Context.getService(PharmacyService.class).getByMasterId(Transaction.class,
			    "sendingId", sending.getId(), 1000, 0);
			for (int i = 0; i < transactionlList.size(); i++) {
				Transaction t = transactionlList.get(i);
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
				    t.getItem().getUuid());
				
				String[] ids = { "itemBatch" };
				String[] values = { t.getItemBatch() };
				
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, ids, values);
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() + t.getQuantity());
				
				item.setVirtualstock(item.getVirtualstock() + t.getQuantity());
				
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
				Context.getService(PharmacyService.class).delete(t);
			}
			
			//3. Update  SendingDetail
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(sending.getSendingDetails());
			
			Transaction transaction;
			for (int i = 0; i < list.size(); i++) {
				SendingDetail o = new SendingDetail();
				
				o.setSendingDetailsQuantity(new Integer(list.get(i).get("sendingDetailsQuantity").toString()));
				o.setSendingDetailsValue(Double.valueOf(list.get(i).get("sendingDetailsValue").toString()));
				o.setSendingItemBatch(list.get(i).get("sendingItemBatch").toString());
				
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, new String[] { "itemBatch" }, new Object[] { o.getSendingItemBatch() });
				
				o.setSendingItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				Integer itemId = new Integer(list.get(i).get("item").toString());
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				o.setItem(item);
				o.setSending(sending);
				
				SendingDetailId sendingDetailId = new SendingDetailId(itemId, sending.getId());
				o.setId(0);
				o.setPk(sendingDetailId);
				Context.getService(PharmacyService.class).upsert(o);
				
				//Create a transaction for this operation
				transaction = new Transaction();
				transaction.setDate(new Date());
				transaction.setQuantity(o.getSendingDetailsQuantity());
				transaction.setSendingId(sending.getId());
				transaction.setItem(item);
				transaction.setPharmacyLocation(itemsLine.getPharmacyLocation());
				transaction.setItemBatch(itemsLine.getItemBatch());
				transaction.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				//TODO
				//transaction.setPersonId((Integer) properties.get("personId"));
				if (sending.getValidationDate() != null)
					transaction.setStatus("APPROVED");
				else
					transaction.setStatus("INIT");
				int transactionType = 5; //disp
				transaction.setTransactionType(transactionType);//disp
				//Upsert the transaction
				Context.getService(PharmacyService.class).upsert(transaction);
				
				//Update the virtual quantities for item and itemsLine
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() - o.getSendingDetailsQuantity());
				item.setVirtualstock(item.getVirtualstock() - o.getSendingDetailsQuantity());
				
				if (sending.getValidationDate() != null) { // update soh
					itemsLine.setItemSoh(itemsLine.getItemSoh() - o.getSendingDetailsQuantity());
					item.setSoh(item.getSoh() - o.getSendingDetailsQuantity());
				}
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
			}
			return ConversionUtil.convertToRepresentation(sending, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(SendingRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	protected void delete(Sending sending, String reason, RequestContext context) throws ResponseException {
		try {
			List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getByMasterId(
			    SendingDetail.class, "sending.id", sending.getId(), 1000, 0);
			for (int i = 0; i < sendingDetailList.size(); i++) {
				SendingDetail o = sendingDetailList.get(i);
				Context.getService(PharmacyService.class).delete(o);
			}
			Context.getService(PharmacyService.class).delete(sending);
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			//2. update all old transactions as canceled
			List<Transaction> transactionlList = Context.getService(PharmacyService.class).getByMasterId(Transaction.class,
			    "sendingId", sending.getId(), 1000, 0);
			for (int i = 0; i < transactionlList.size(); i++) {
				Transaction t = transactionlList.get(i);
				
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
				    t.getItem().getUuid());
				
				String[] ids = { "itemBatch" };
				String[] values = { t.getItemBatch() };
				
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, ids, values);
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() + t.getQuantity());
				
				item.setVirtualstock(item.getVirtualstock() + t.getQuantity());
				
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
				
				Context.getService(PharmacyService.class).delete(t);
			}
			
		}
		catch (ParseException ex) {
			Logger.getLogger(TransactionRequestResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	@Override
	public void purge(Sending sending, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(sending);
	}
	
	private Sending constructOrder(String uuid, SimpleObject properties) throws ParseException {
		Sending sending;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Customer customer = null;
		if (properties.get("customer") != null) {
			Integer customerId = properties.get("customer");
			customer = (Customer) Context.getService(PharmacyService.class).getEntityByid(Customer.class, "id", customerId);
		}
		Person patient = null;
		if (properties.get("person") != null) {
			String patientId = properties.get("person");
			patient = (Person) Context.getService(PharmacyService.class).getEntityByUuid(Person.class, patientId);
		}
		CustomerType customerType = null;
		if (properties.get("customerType") != null) {
			Integer customerTypeId = properties.get("customerType");
			customerType = (CustomerType) Context.getService(PharmacyService.class).getEntityByid(CustomerType.class, "id",
			    customerTypeId);
		}
		
		if (uuid != null) {
			sending = (Sending) Context.getService(PharmacyService.class).getEntityByUuid(Sending.class, uuid);
			if (sending == null) {
				throw new IllegalPropertyException("Sending not exist");
			}
			
			if (properties.get("date") != null) {
				sending.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			if (properties.get("validationDate") != null) {
				sending.setValidationDate(simpleDateFormat.parse(properties.get("validationDate").toString()));
			}
			if (properties.get("sendingAmount") != null) {
				sending.setSendingAmount(Double.valueOf(properties.get("sendingAmount").toString()));
			}
			
			if (properties.get("sendingDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("sendingDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				sending.setSendingDetails(set);
			}
			
			sending.setCustomerType(customerType);
			
			if (properties.get("person") != null) {
				sending.setPerson(patient);
			} else if (properties.get("customer") != null) {
				sending.setCustomer(customer);
			}
			
		} else {
			sending = new Sending();
			sending.setCustomerType(customerType);
			if (properties.get("person") != null) {
				sending.setPerson(patient);
			} else {
				sending.setCustomer(customer);
			}
			if (properties.get("date") != null) {
				sending.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			if (properties.get("validationDate") != null) {
				sending.setValidationDate(simpleDateFormat.parse(properties.get("validationDate").toString()));
			}
			if (properties.get("sendingAmount") != null) {
				sending.setSendingAmount(Double.valueOf(properties.get("sendingAmount").toString()));
			}
			if (properties.get("sendingDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("sendingDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				sending.setSendingDetails(set);
			}
		}
		
		return sending;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/sending";
	}
	
}
