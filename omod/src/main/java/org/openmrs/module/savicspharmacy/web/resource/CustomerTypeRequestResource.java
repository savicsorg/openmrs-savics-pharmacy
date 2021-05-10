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
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.savicspharmacy.api.entity.CustomerType;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/customerType", supportedClass = CustomerType.class, supportedOpenmrsVersions = { "2.*.*" })
public class CustomerTypeRequestResource extends DataDelegatingCrudResource<CustomerType> {
	
	@Override
	public CustomerType newDelegate() {
		return new CustomerType();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<CustomerType> districtList = Context.getService(PharmacyService.class).getAll(CustomerType.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(districtList.toString());
		return new AlreadyPaged<CustomerType>(context, districtList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<CustomerType> districtList = Context.getService(PharmacyService.class).doSearch(CustomerType.class, "name",
		    value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<CustomerType>(context, districtList, false);
	}
	
	@Override
	public CustomerType getByUniqueId(String uuid) {
		
		return (CustomerType) Context.getService(PharmacyService.class).getEntityByUuid(CustomerType.class, uuid);
	}
	
	@Override
	public CustomerType save(CustomerType customerType) {
		return (CustomerType) Context.getService(PharmacyService.class).upsert(customerType);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null) {
			throw new ConversionException("Required properties: name");
		}
		CustomerType customerType = this.constructDistrict(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(customerType);
		return ConversionUtil.convertToRepresentation(customerType, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		CustomerType customerType = this.constructDistrict(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(customerType);
		return ConversionUtil.convertToRepresentation(customerType, context.getRepresentation());
	}
	
	@Override
	protected void delete(CustomerType customerType, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(customerType);
	}
	
	@Override
	public void purge(CustomerType customerType, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(customerType);
	}
	
	private CustomerType constructDistrict(String uuid, SimpleObject properties) {
		CustomerType customerType;
		
		if (uuid != null) {
			customerType = (CustomerType) Context.getService(PharmacyService.class)
			        .getEntityByUuid(CustomerType.class, uuid);
			if (customerType == null) {
				throw new IllegalPropertyException("customerType not exist");
			}
			
			if (properties.get("name") != null) {
				customerType.setName((String) properties.get("name"));
			}
			
		} else {
			customerType = new CustomerType();
			if (properties.get("name") == null) {
				throw new IllegalPropertyException("Required parameters: name");
			}
			customerType.setName((String) properties.get("name"));
		}
		
		return customerType;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/customerType";
	}
	
}
