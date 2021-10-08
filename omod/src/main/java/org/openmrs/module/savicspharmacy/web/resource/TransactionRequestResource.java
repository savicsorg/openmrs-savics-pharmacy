package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/transaction", supportedClass = Transaction.class, supportedOpenmrsVersions = { "2.*.*" })
public class TransactionRequestResource extends DataDelegatingCrudResource<Transaction> {
	
	@Override
	public Transaction newDelegate() {
		return new Transaction();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("personId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("reason");
			description.addProperty("item");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("personId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("reason");
			description.addProperty("item");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("personId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("reason");
			description.addProperty("item");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Transaction> transactionList = Context.getService(PharmacyService.class).getAll(Transaction.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(transactionList.toString());
		return new AlreadyPaged<Transaction>(context, transactionList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("item");
		Integer itemValue = Integer.parseInt(context.getParameter("item"));
		List<Transaction> transactionList;
		transactionList = Context.getService(PharmacyService.class).getByMasterId(Transaction.class, "item.id", itemValue,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Transaction>(context, transactionList, false);
	}
	
	@Override
	public Transaction getByUniqueId(String uuid) {
		
		return (Transaction) Context.getService(PharmacyService.class).getEntityByUuid(Transaction.class, uuid);
	}
	
	@Override
	public Transaction save(Transaction transaction) {
		return (Transaction) Context.getService(PharmacyService.class).upsert(transaction);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("item") == null || propertiesToCreate.get("pharmacyLocation") == null
		        || propertiesToCreate.get("transactionType") == null) {
			throw new ConversionException("Required properties: Item, PharmacyLocation, TransactionType");
		}
		Transaction transaction;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			transaction = this.constructTransaction(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(transaction);
			
			/**
			 * Starting updating itemsline virtual quantity
			 */
			String itemsLineUuid = propertiesToCreate.get("selectedBatchUuid");
			
			ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByUuid(ItemsLine.class,
			    itemsLineUuid);
			itemsLine.setItemExpiryDate(simpleDateFormat.parse(propertiesToCreate.get("itemExpiryDate").toString()));
			
			Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
			    itemsLine.getItem().getUuid());
			
			if ("padj".equals(propertiesToCreate.get("transactionTypeCode").toString())) {
				itemsLine
				        .setItemVirtualstock(itemsLine.getItemVirtualstock() + (Integer) propertiesToCreate.get("quantity"));
				item.setVirtualstock(item.getVirtualstock() + (Integer) propertiesToCreate.get("quantity"));
			} else {
				itemsLine
				        .setItemVirtualstock(itemsLine.getItemVirtualstock() - (Integer) propertiesToCreate.get("quantity"));
				item.setVirtualstock(item.getVirtualstock() - (Integer) propertiesToCreate.get("quantity"));
			}
			Context.getService(PharmacyService.class).upsert(itemsLine);
			Context.getService(PharmacyService.class).upsert(item);
			//End of itemsline virtual quantity update
			
			return ConversionUtil.convertToRepresentation(transaction, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(TransactionRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Transaction transaction;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			transaction = this.constructTransaction(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(transaction);
			
			if ("VALID".equals(transaction.getStatus())) {//If a validation
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
				    transaction.getItem().getUuid());
				
				String[] ids = { "itemBatch" };
				String[] values = { transaction.getItemBatch() };
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, ids, values);
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				
				if (2 == transaction.getTransactionType() || 7 == transaction.getTransactionType()) {//padj or pstocktake
					item.setSoh(item.getSoh() + transaction.getQuantity());
					itemsLine.setItemSoh(itemsLine.getItemSoh() + transaction.getQuantity());
				} else if (1 == transaction.getTransactionType() || 6 == transaction.getTransactionType()) {//nadj or nstocktake
					item.setSoh(item.getSoh() - transaction.getQuantity());
					itemsLine.setItemSoh(itemsLine.getItemSoh() - transaction.getQuantity());
				}
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
				//End of item and itemline real  quantity update
			} else if ("REJECT".equals(transaction.getStatus())) {//If a cancelation
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
				    transaction.getItem().getUuid());
				
				String[] ids = { "itemBatch" };
				String[] values = { transaction.getItemBatch() };
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(
				    ItemsLine.class, ids, values);
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(itemsLine.getItemExpiryDate().toString()));
				
				if (2 == transaction.getTransactionType() || 7 == transaction.getTransactionType()) {//padj
					item.setVirtualstock(item.getVirtualstock() - transaction.getQuantity());
					itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() - transaction.getQuantity());
				} else if (1 == transaction.getTransactionType() || 6 == transaction.getTransactionType()) {//nadj
					item.setVirtualstock(item.getVirtualstock() + transaction.getQuantity());
					itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock() + transaction.getQuantity());
				}
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
				//End of item and itemline real  quantity update
			} else if (propertiesToUpdate.get("selectedBatchUuid") != null) {
				/**
				 * Starting updating itemsline virtual quantity
				 */
				String itemsLineUuid = propertiesToUpdate.get("selectedBatchUuid");
				
				ItemsLine itemsLine = (ItemsLine) Context.getService(PharmacyService.class).getEntityByUuid(ItemsLine.class,
				    itemsLineUuid);
				itemsLine.setItemExpiryDate(simpleDateFormat.parse(propertiesToUpdate.get("itemExpiryDate").toString()));
				
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByUuid(Item.class,
				    itemsLine.getItem().getUuid());
				
				if ("padj".equals(propertiesToUpdate.get("transactionTypeCode").toString())) {
					if ("padj".equals(propertiesToUpdate.get("oldTransactionType").toString())) {
						itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
						        - (Integer) propertiesToUpdate.get("oldQuantity"));
						item.setVirtualstock(item.getVirtualstock() - (Integer) propertiesToUpdate.get("oldQuantity"));
						
					} else if ("nadj".equals(propertiesToUpdate.get("oldTransactionType").toString())) {
						itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
						        + (Integer) propertiesToUpdate.get("oldQuantity"));
						item.setVirtualstock(item.getVirtualstock() + (Integer) propertiesToUpdate.get("oldQuantity"));
					}
					itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
					        + (Integer) propertiesToUpdate.get("quantity"));
					item.setVirtualstock(item.getVirtualstock() + (Integer) propertiesToUpdate.get("quantity"));
					
				} else if ("nadj".equals(propertiesToUpdate.get("transactionTypeCode").toString())) {
					if ("padj".equals(propertiesToUpdate.get("oldTransactionType").toString())) {
						itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
						        - (Integer) propertiesToUpdate.get("oldQuantity"));
						item.setVirtualstock(item.getVirtualstock() - (Integer) propertiesToUpdate.get("oldQuantity"));
						
					} else if ("nadj".equals(propertiesToUpdate.get("oldTransactionType").toString())) {
						itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
						        + (Integer) propertiesToUpdate.get("oldQuantity"));
						item.setVirtualstock(item.getVirtualstock() + (Integer) propertiesToUpdate.get("oldQuantity"));
					}
					
					itemsLine.setItemVirtualstock(itemsLine.getItemVirtualstock()
					        - (Integer) propertiesToUpdate.get("quantity"));
					item.setVirtualstock(item.getVirtualstock() - (Integer) propertiesToUpdate.get("quantity"));
					
				}
				Context.getService(PharmacyService.class).upsert(itemsLine);
				Context.getService(PharmacyService.class).upsert(item);
			}
			
			return ConversionUtil.convertToRepresentation(transaction, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(TransactionRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	protected void delete(Transaction transaction, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transaction);
	}
	
	@Override
	public void purge(Transaction transaction, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transaction);
	}
	
	private Transaction constructTransaction(String uuid, SimpleObject properties) throws ParseException {
		Transaction transaction;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int transactionType = 2;
		if (properties.get("transactionType") != null) {
			transactionType = (Integer) properties.get("transactionType");
		}
		PharmacyLocation pharmacyLocation = null;
		if (properties.get("pharmacyLocation") != null) {
			Integer pharmacyLocationId = (Integer) properties.get("pharmacyLocation");
			pharmacyLocation = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByid(
			    PharmacyLocation.class, "id", pharmacyLocationId);
		}
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = (Integer) properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
		}
		if (uuid != null) {
			transaction = (Transaction) Context.getService(PharmacyService.class).getEntityByUuid(Transaction.class, uuid);
			if (transaction == null) {
				throw new IllegalPropertyException("transaction not exist");
			}
			
			if (properties.get("date") != null) {
				transaction.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			
			if (properties.get("quantity") != null) {
				transaction.setQuantity((Integer) properties.get("quantity"));
			}
			
			if (properties.get("itemBatch") != null) {
				transaction.setItemBatch((String) properties.get("itemBatch"));
			}
			
			if (properties.get("itemExpiryDate") != null) {
				transaction.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			}
			
			if (properties.get("personId") != null) {
				transaction.setPersonId((Integer) properties.get("personId"));
			}
			
			if (properties.get("amount") != null) {
				transaction.setAmount((Double) properties.get("amount"));
			}
			
			if (properties.get("status") != null) {
				transaction.setStatus((String) properties.get("status"));
			}
			
			if (properties.get("sendingId") != null) {
				transaction.setSendingId((Integer) properties.get("sendingId"));
			}
			
			if (properties.get("receptionId") != null) {
				transaction.setReceptionId((Integer) properties.get("receptionId"));
			}
			
			if (properties.get("stocktakeId") != null) {
				transaction.setStocktakeId((Integer) properties.get("stocktakeId"));
			}
			
			if (properties.get("adjustmentDate") != null) {
				transaction.setAdjustmentDate(simpleDateFormat.parse(properties.get("adjustmentDate").toString()));
			}
			
			if (properties.get("reason") != null) {
				transaction.setReason(properties.get("reason").toString());
			}
			
			transaction.setTransactionType(transactionType);
			transaction.setItem(item);
			transaction.setPharmacyLocation(pharmacyLocation);
		} else {
			transaction = new Transaction();
			if (properties.get("item") == null || properties.get("pharmacyLocation") == null
			        || properties.get("transactionType") == null) {
				throw new ConversionException("Required properties: Item, PharmacyLocation, TransactionType");
			}
			
			transaction.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			
			transaction.setQuantity((Integer) properties.get("quantity"));
			
			transaction.setItemBatch((String) properties.get("itemBatch"));
			
			transaction.setItemExpiryDate(simpleDateFormat.parse(properties.get("itemExpiryDate").toString()));
			
			transaction.setPersonId((Integer) properties.get("personId"));
			
			transaction.setAmount((Double) properties.get("amount"));
			
			transaction.setStatus((String) properties.get("status"));
			transaction.setReason((String) properties.get("reason"));
			
			transaction.setSendingId((Integer) properties.get("sendingId"));
			
			transaction.setReceptionId((Integer) properties.get("receptionId"));
			
			transaction.setStocktakeId((Integer) properties.get("stocktakeId"));
			
			transaction.setAdjustmentDate(simpleDateFormat.parse(properties.get("adjustmentDate").toString()));
			transaction.setTransactionType(transactionType);
			transaction.setItem(item);
			transaction.setPharmacyLocation(pharmacyLocation);
		}
		
		return transaction;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/transaction";
	}
	
}
