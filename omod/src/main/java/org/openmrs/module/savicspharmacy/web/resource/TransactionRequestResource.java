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
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyLocation;
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.openmrs.module.savicspharmacy.api.entity.TransactionType;
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
			description.addProperty("transactionTypeId");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("patientId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("item");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("transactionTypeId");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("patientId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("item");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("transactionTypeId");
			description.addProperty("quantity");
			description.addProperty("itemBatch");
			description.addProperty("date");
			description.addProperty("itemExpiryDate");
			description.addProperty("patientId");
			description.addProperty("amount");
			description.addProperty("status");
			description.addProperty("sendingId");
			description.addProperty("receptionId");
			description.addProperty("stocktakeId");
			description.addProperty("adjustmentDate");
			description.addProperty("pharmacyLocation");
			description.addProperty("transactionType");
			description.addProperty("item");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		System.out.println("---- doGetAll ");
		List<Transaction> transactionList = Context.getService(PharmacyService.class).getAll(Transaction.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(transactionList.toString());
		return new AlreadyPaged<Transaction>(context, transactionList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Transaction> transactionList = Context.getService(PharmacyService.class).doSearch(Transaction.class, "name",
		    value, context.getLimit(), context.getStartIndex());
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
		if (propertiesToCreate.get("Item") == null || propertiesToCreate.get("PharmacyLocation") == null
		        || propertiesToCreate.get("TransactionType") == null) {
			throw new ConversionException("Required properties: Item, PharmacyLocation, TransactionType");
		}
		Transaction transaction = this.constructTransaction(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(transaction);
		return ConversionUtil.convertToRepresentation(transaction, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Transaction transaction = this.constructTransaction(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(transaction);
		return ConversionUtil.convertToRepresentation(transaction, context.getRepresentation());
	}
	
	@Override
	protected void delete(Transaction transaction, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transaction);
	}
	
	@Override
	public void purge(Transaction transaction, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transaction);
	}
	
	private Transaction constructTransaction(String uuid, SimpleObject properties) {
		Transaction transaction;
		TransactionType transactionType = null;
		if (properties.get("transactionType") != null) {
			Integer transactionTypeId = properties.get("transactionType");
			transactionType = (TransactionType) Context.getService(PharmacyService.class).getEntityByid(
			    TransactionType.class, "id", transactionTypeId);
		}
		PharmacyLocation pharmacyLocation = null;
		if (properties.get("pharmacyLocation") != null) {
			Integer pharmacyLocationId = properties.get("pharmacyLocation");
			pharmacyLocation = (PharmacyLocation) Context.getService(PharmacyService.class).getEntityByid(
			    PharmacyLocation.class, "id", pharmacyLocationId);
		}
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
		}
		if (uuid != null) {
			transaction = (Transaction) Context.getService(PharmacyService.class).getEntityByUuid(Transaction.class, uuid);
			if (transaction == null) {
				throw new IllegalPropertyException("transaction not exist");
			}
			
			if (properties.get("date") != null) {
				transaction.setDate((Date) properties.get("date"));
			}
			
			if (properties.get("quantity") != null) {
				transaction.setQuantity((Integer) properties.get("quantity"));
			}
			
			if (properties.get("itemBatch") != null) {
				transaction.setItemBatch((String) properties.get("itemBatch"));
			}
			
			if (properties.get("itemExpiryDate") != null) {
				transaction.setItemExpiryDate((Date) properties.get("itemExpiryDate"));
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
				transaction.setAdjustmentDate((Date) properties.get("adjustmentDate"));
			}
			
			transaction.setTransactionType(transactionType);
			transaction.setItem(item);
			transaction.setPharmacyLocation(pharmacyLocation);
		} else {
			transaction = new Transaction();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, code");
			}
			
			transaction.setDate((Date) properties.get("date"));
			
			transaction.setQuantity((Integer) properties.get("quantity"));
			
			transaction.setItemBatch((String) properties.get("itemBatch"));
			
			transaction.setItemExpiryDate((Date) properties.get("itemExpiryDate"));
			
			transaction.setPersonId((Integer) properties.get("personId"));
			
			transaction.setAmount((Double) properties.get("amount"));
			
			transaction.setStatus((String) properties.get("status"));
			
			transaction.setSendingId((Integer) properties.get("sendingId"));
			
			transaction.setReceptionId((Integer) properties.get("receptionId"));
			
			transaction.setStocktakeId((Integer) properties.get("stocktakeId"));
			
			transaction.setAdjustmentDate((Date) properties.get("adjustmentDate"));
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
