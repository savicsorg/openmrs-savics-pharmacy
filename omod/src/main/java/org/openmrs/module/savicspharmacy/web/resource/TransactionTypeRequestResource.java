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
import org.openmrs.module.savicspharmacy.api.entity.TransactionType;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/transactionType", supportedClass = TransactionType.class, supportedOpenmrsVersions = { "2.*.*" })
public class TransactionTypeRequestResource extends DataDelegatingCrudResource<TransactionType> {
	
	@Override
	public TransactionType newDelegate() {
		return new TransactionType();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("code");
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
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<TransactionType> transactionTypeList = Context.getService(PharmacyService.class).getAll(TransactionType.class,
		    context.getLimit(), context.getStartIndex());
		System.out.println(transactionTypeList.toString());
		return new AlreadyPaged<TransactionType>(context, transactionTypeList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String value = context.getParameter("name");
		List<TransactionType> transactionTypeList = Context.getService(PharmacyService.class).doSearch(
		    TransactionType.class, "name", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<TransactionType>(context, transactionTypeList, false);
	}
	
	@Override
	public TransactionType getByUniqueId(String uuid) {
		
		return (TransactionType) Context.getService(PharmacyService.class).getEntityByUuid(TransactionType.class, uuid);
	}
	
	@Override
	public TransactionType save(TransactionType transactionType) {
		return (TransactionType) Context.getService(PharmacyService.class).upsert(transactionType);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("name") == null || propertiesToCreate.get("code") == null) {
			throw new ConversionException("Required properties: name, code");
		}
		TransactionType transactionType = this.constructTransactionType(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(transactionType);
		return ConversionUtil.convertToRepresentation(transactionType, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		TransactionType transactionType = this.constructTransactionType(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(transactionType);
		return ConversionUtil.convertToRepresentation(transactionType, context.getRepresentation());
	}
	
	@Override
	protected void delete(TransactionType transactionType, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transactionType);
	}
	
	@Override
	public void purge(TransactionType transactionType, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(transactionType);
	}
	
	private TransactionType constructTransactionType(String uuid, SimpleObject properties) {
		TransactionType transactionType;
		
		if (uuid != null) {
			transactionType = (TransactionType) Context.getService(PharmacyService.class).getEntityByUuid(
			    TransactionType.class, uuid);
			if (transactionType == null) {
				throw new IllegalPropertyException("transactionType not exist");
			}
			
			if (properties.get("name") != null) {
				transactionType.setName((String) properties.get("name"));
			}
			
			if (properties.get("code") != null) {
				transactionType.setCode((String) properties.get("code"));
			}
			
			if (properties.get("id") != null) {
				transactionType.setId((Integer) properties.get("id"));
			}
		} else {
			transactionType = new TransactionType();
			if (properties.get("name") == null || properties.get("code") == null || properties.get("id") == null) {
				throw new IllegalPropertyException("Required parameters: name, code, id");
			}
			transactionType.setName((String) properties.get("name"));
			transactionType.setCode((String) properties.get("code"));
			transactionType.setId((Integer) properties.get("id"));
		}
		return transactionType;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/transactionType";
	}
	
}
