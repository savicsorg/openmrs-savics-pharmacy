package org.openmrs.module.savicspharmacy.web.resource;

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
import org.openmrs.module.savicspharmacy.api.entity.Customer;
import org.openmrs.module.savicspharmacy.api.entity.CustomerType;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/customer", supportedClass = Customer.class, supportedOpenmrsVersions = { "2.*.*" })
public class CustomerRequestResource extends DataDelegatingCrudResource<Customer> {
	
	@Override
	public Customer newDelegate() {
		return new Customer();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addProperty("patientId");
			description.addProperty("customerType");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addProperty("patientId");
			description.addProperty("customerType");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addProperty("patientId");
			description.addProperty("customerType");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		System.out.println("---- doGetAll ");
		List<Customer> customerList = Context.getService(PharmacyService.class).getAll(Customer.class, context.getLimit(),
		    context.getStartIndex());
		System.out.println(customerList.toString());
		return new AlreadyPaged<Customer>(context, customerList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Customer> customerList = Context.getService(PharmacyService.class).doSearch(Customer.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Customer>(context, customerList, false);
	}
	
	@Override
	public Customer getByUniqueId(String uuid) {
		
		return (Customer) Context.getService(PharmacyService.class).getEntityByUuid(Customer.class, uuid);
	}
	
	@Override
	public Customer save(Customer customer) {
		return (Customer) Context.getService(PharmacyService.class).upsert(customer);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		System.out.println("-----------------------------");
		System.out.println(propertiesToCreate);
		System.out.println();
		Customer customer = this.constructCustomer(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(customer);
		return ConversionUtil.convertToRepresentation(customer, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Customer customer = this.constructCustomer(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(customer);
		return ConversionUtil.convertToRepresentation(customer, context.getRepresentation());
	}
	
	@Override
	protected void delete(Customer customer, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(customer);
	}
	
	@Override
	public void purge(Customer customer, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(customer);
	}
	
	private Customer constructCustomer(String uuid, SimpleObject properties) {
		Customer customer;
		CustomerType customerType = null;
		if (properties.get("customerType") != null) {
			Integer customerTypeId = properties.get("customerType");
			customerType = (CustomerType) Context.getService(PharmacyService.class).getEntityByid(CustomerType.class,
			    "customerType", customerTypeId);
		}
		if (uuid != null) {
			customer = (Customer) Context.getService(PharmacyService.class).getEntityByUuid(Customer.class, uuid);
			if (customer == null) {
				throw new IllegalPropertyException("customer not exist");
			}
			
			if (properties.get("name") != null) {
				customer.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				customer.setCode((String) properties.get("code"));
			}
			
			if (properties.get("address") != null) {
				customer.setAddress((String) properties.get("address"));
			}
			
			if (properties.get("email") != null) {
				customer.setEmail((String) properties.get("email"));
			}
			
			if (properties.get("tel") != null) {
				customer.setTel((String) properties.get("tel"));
			}
			
			if (properties.get("patientId") != null) {
				customer.setPatientId((Integer) properties.get("patientId"));
			}
			
			customer.setCustomerType(customerType);
		} else {
			customer = new Customer();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, code");
			}
			customer.setName((String) properties.get("name"));
			customer.setCode((String) properties.get("code"));
			customer.setAddress((String) properties.get("address"));
			customer.setEmail((String) properties.get("email"));
			customer.setTel((String) properties.get("tel"));
			customer.setPatientId((Integer) properties.get("patientId"));
			customer.setCustomerType(customerType);
		}
		
		return customer;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/customer";
	}
	
}
