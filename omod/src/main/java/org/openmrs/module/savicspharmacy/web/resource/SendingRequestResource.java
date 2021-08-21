package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.savicspharmacy.api.entity.Customer;
import org.openmrs.module.savicspharmacy.api.entity.Item;
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
import org.openmrs.module.savicspharmacy.api.entity.Transaction;
import org.openmrs.module.savicspharmacy.api.entity.TransactionType;
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
			description.addProperty("sendingDetails");
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
			description.addProperty("sendingDetails");
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
			description.addProperty("sendingDetails");
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
			Context.getService(PharmacyService.class).upsert(sending);
			for (int i = 0; i < sending.getSendingDetails().size(); i++) {
				SendingDetail o = (SendingDetail) sending.getSendingDetails().toArray()[i];
				Item item = null;
				Integer itemId = o.getItem().getId();
				item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				o.setItem(item);
				o.setSending(sending);
				Context.getService(PharmacyService.class).upsert(o);
			}
			return ConversionUtil.convertToRepresentation(sending, context.getRepresentation());
		}
		catch (ParseException e) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Sending sending;
		try {
			sending = this.constructOrder(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(sending);
			List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getByMasterId(
			    SendingDetail.class, "sending.id", sending.getId(), 1000, 0);
			for (int i = 0; i < sendingDetailList.size(); i++) {
				SendingDetail o = sendingDetailList.get(i);
				Context.getService(PharmacyService.class).delete(o);
			}
			for (int i = 0; i < sending.getSendingDetails().size(); i++) {
				SendingDetail o = (SendingDetail) sending.getSendingDetails().toArray()[i];
				Item item = null;
				Integer itemId = o.getItem().getId();
				item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
				o.setItem(item);
				o.setSending(sending);
				Context.getService(PharmacyService.class).upsert(o);
			}
			return ConversionUtil.convertToRepresentation(sending, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	protected void delete(Sending sending, String reason, RequestContext context) throws ResponseException {
		List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getByMasterId(SendingDetail.class,
		    "sending.id", sending.getId(), 1000, 0);
		for (int i = 0; i < sendingDetailList.size(); i++) {
			SendingDetail o = sendingDetailList.get(i);
			Context.getService(PharmacyService.class).delete(o);
		}
		Context.getService(PharmacyService.class).delete(sending);
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
		
		if (uuid != null) {
			sending = (Sending) Context.getService(PharmacyService.class).getEntityByUuid(Sending.class, uuid);
			if (sending == null) {
				throw new IllegalPropertyException("Sending not exist");
			}
			
			if (properties.get("date") != null) {
				sending.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			
			if (properties.get("sendingAmount") != null) {
				sending.setSendingAmount(Double.valueOf(properties.get("sendingAmount").toString()));
			}
                        
                        if (properties.get("sendingDetails") != null) {
				sending.setSendingDetails((Set<SendingDetail>)properties.get("sendingDetails"));
			}
			
		} else {
			sending = new Sending();
			if (properties.get("person") != null)
				sending.setPerson(patient);
			else
				sending.setCustomer(customer);
			if (properties.get("date") != null) {
				sending.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			
			if (properties.get("sendingAmount") != null) {
				sending.setSendingAmount(Double.valueOf(properties.get("sendingAmount").toString()));
			}
                        if (properties.get("sendingDetails") != null) {
				sending.setSendingDetails((Set<SendingDetail>)properties.get("sendingDetails"));
			}
		}
		
		return sending;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/sending";
	}
	
}
