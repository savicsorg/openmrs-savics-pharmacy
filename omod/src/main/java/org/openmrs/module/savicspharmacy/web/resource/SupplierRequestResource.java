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
import org.openmrs.module.savicspharmacy.api.entity.Supplier;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/supplier", supportedClass = Supplier.class, supportedOpenmrsVersions = { "2.*.*" })
public class SupplierRequestResource extends DataDelegatingCrudResource<Supplier> {
	
	@Override
	public Supplier newDelegate() {
		return new Supplier();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("code");
			description.addProperty("name");
			description.addProperty("address");
			description.addProperty("email");
			description.addProperty("tel");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Supplier> supplierList = Context.getService(PharmacyService.class).getAll(Supplier.class, context.getLimit(),
		    context.getStartIndex());
		System.out.println(supplierList.toString());
		return new AlreadyPaged<Supplier>(context, supplierList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<Supplier> supplierList = Context.getService(PharmacyService.class).doSearch(Supplier.class, "name", value,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<Supplier>(context, supplierList, false);
	}
	
	@Override
	public Supplier getByUniqueId(String uuid) {
		
		return (Supplier) Context.getService(PharmacyService.class).getEntityByUuid(Supplier.class, uuid);
	}
	
	@Override
	public Supplier save(Supplier supplier) {
		return (Supplier) Context.getService(PharmacyService.class).upsert(supplier);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		Supplier supplier = this.constructSupplier(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(supplier);
		return ConversionUtil.convertToRepresentation(supplier, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		Supplier supplier = this.constructSupplier(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(supplier);
		return ConversionUtil.convertToRepresentation(supplier, context.getRepresentation());
	}
	
	@Override
	protected void delete(Supplier supplier, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(supplier);
	}
	
	@Override
	public void purge(Supplier supplier, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(supplier);
	}
	
	private Supplier constructSupplier(String uuid, SimpleObject properties) {
		Supplier supplier;
		
		if (uuid != null) {
			supplier = (Supplier) Context.getService(PharmacyService.class).getEntityByUuid(Supplier.class, uuid);
			if (supplier == null) {
				throw new IllegalPropertyException("supplier not exist");
			}
			
			if (properties.get("name") != null) {
				supplier.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				supplier.setCode((String) properties.get("code"));
			}
			
			if (properties.get("address") != null) {
				supplier.setAddress((String) properties.get("address"));
			}
			
			if (properties.get("email") != null) {
				supplier.setEmail((String) properties.get("email"));
			}
			
			if (properties.get("tel") != null) {
				supplier.setTel((String) properties.get("tel"));
			}
			
		} else {
			supplier = new Supplier();
			if (properties.get("name") == null || properties.get("code") == null) {
				throw new IllegalPropertyException("Required parameters: name, name");
			}
			supplier.setName((String) properties.get("name"));
			supplier.setCode((String) properties.get("code"));
			supplier.setAddress((String) properties.get("address"));
			supplier.setEmail((String) properties.get("email"));
			supplier.setTel((String) properties.get("tel"));
		}
		
		return supplier;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/supplier";
	}
	
}
