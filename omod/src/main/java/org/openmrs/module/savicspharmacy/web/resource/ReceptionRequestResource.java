package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyOrder;
import org.openmrs.module.savicspharmacy.api.entity.Reception;
import org.openmrs.module.savicspharmacy.api.entity.Supplier;
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
			return ConversionUtil.convertToRepresentation(reception, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	protected void delete(Reception reception, String reason, RequestContext context) throws ResponseException {
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
			Integer orderId = properties.get("pharmacyOrder");
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByid(PharmacyOrder.class, "id", orderId);			
		}
		
		if (uuid != null) {
			reception = (Reception) Context.getService(PharmacyService.class).getEntityByUuid(Reception.class, uuid);
			if (reception == null) {
				throw new IllegalPropertyException("Reception not exist");
			}
			if (properties.get("date") != null) {
				reception.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			if (order != null)
				reception.setPharmacyOrder(order);
		} else {
			reception = new Reception();
			reception.setPerson(Context.getUserContext().getAuthenticatedUser().getPerson());
			reception.setDate(new Date());
			if (order != null) {
                            reception.setPharmacyOrder(order);
                            order.setDateReception(reception.getDate());
                            Context.getService(PharmacyService.class).upsert(order);
			}
		}
		return reception;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/reception";
	}
	
}
