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
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import org.openmrs.Person;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyOrder;
import org.openmrs.module.savicspharmacy.api.entity.Supplier;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/order", supportedClass = PharmacyOrder.class, supportedOpenmrsVersions = { "2.*.*" })
public class OrderRequestResource extends DelegatingCrudResource<PharmacyOrder> {
	
	@Override
	public PharmacyOrder newDelegate() {
		return new PharmacyOrder();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("amount");
			description.addProperty("dateApprobation");
			description.addProperty("dateReception");
			description.addProperty("person");
			description.addProperty("supplier");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("amount");
			description.addProperty("dateApprobation");
			description.addProperty("dateReception");
			description.addProperty("person");
			description.addProperty("supplier");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("amount");
			description.addProperty("dateApprobation");
			description.addProperty("dateReception");
			description.addProperty("person");
			description.addProperty("supplier");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<PharmacyOrder> agentList = Context.getService(PharmacyService.class).getAll(PharmacyOrder.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<PharmacyOrder>(context, agentList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<PharmacyOrder> agentList = Context.getService(PharmacyService.class).doSearch(PharmacyOrder.class, "name",
		    value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<PharmacyOrder>(context, agentList, false);
	}
	
	@Override
	public PharmacyOrder getByUniqueId(String uuid) {
		
		return (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByUuid(PharmacyOrder.class, uuid);
	}
	
	@Override
	public PharmacyOrder save(PharmacyOrder order) {
		return (PharmacyOrder) Context.getService(PharmacyService.class).upsert(order);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null) {
			throw new ConversionException("Required properties: name");
		}
		
		PharmacyOrder order = this.constructAgent(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(order);
		return ConversionUtil.convertToRepresentation(order, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		PharmacyOrder order = this.constructAgent(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(order);
		return ConversionUtil.convertToRepresentation(order, context.getRepresentation());
	}
	
	@Override
	protected void delete(PharmacyOrder order, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(order);
	}
	
	@Override
	public void purge(PharmacyOrder order, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(order);
	}
	
	private PharmacyOrder constructAgent(String uuid, SimpleObject properties) {
		PharmacyOrder order;
		
		Person person = null;
		if (properties.get("person") != null) {
			Integer personId = properties.get("person");
			person = (Person) Context.getService(PharmacyService.class).getEntityByid(Person.class, "person", personId);
		}
		
		Supplier supplier = null;
		if (properties.get("supplier") != null) {
			Integer supplierId = properties.get("supplier");
			supplier = (Supplier) Context.getService(PharmacyService.class).getEntityByid(Supplier.class, "supplier",
			    supplierId);
		}
		
		if (uuid != null) {
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByUuid(PharmacyOrder.class, uuid);
			if (order == null) {
				throw new IllegalPropertyException("Items line not exist");
			}
			
			if (properties.get("name") != null) {
				order.setName((String) properties.get("name"));
			}
			
			if (properties.get("date") != null) {
				order.setDate((Date) properties.get("date"));
			}
			
			if (properties.get("dateApprobation") != null) {
				order.setDateApprobation((Date) properties.get("dateApprobation"));
			}
			
			if (properties.get("dateReception") != null) {
				order.setDateReception((Date) properties.get("dateReception"));
			}
			
			if (properties.get("amount") != null) {
				order.setAmount((Double) properties.get("amount"));
			}
			
			if (properties.get("person") != null) {
				order.setPerson(person);
			}
			
			if (properties.get("supplierId") != null) {
				order.setSupplier(supplier);
			}
			
		} else {
			order = new PharmacyOrder();
			if (properties.get("name") == null) {
				throw new IllegalPropertyException("Required parameters: name");
			}
			order.setName((String) properties.get("name"));
			order.setDate((Date) properties.get("date"));
			order.setDateApprobation((Date) properties.get("dateApprobation"));
			order.setDateReception((Date) properties.get("dateReception"));
			order.setAmount((Double) properties.get("amount"));
			order.setPerson(person);
			order.setSupplier(supplier);
		}
		
		return order;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/order";
	}
	
}
