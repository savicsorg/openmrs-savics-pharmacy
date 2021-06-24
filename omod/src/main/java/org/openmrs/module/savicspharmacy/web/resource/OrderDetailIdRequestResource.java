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
import org.openmrs.module.savicspharmacy.api.entity.OrderDetailId;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/orderDetailId", supportedClass = OrderDetailId.class, supportedOpenmrsVersions = { "2.*.*" })
public class OrderDetailIdRequestResource extends DelegatingCrudResource<OrderDetailId> {
	
	@Override
	public OrderDetailId newDelegate() {
		return new OrderDetailId();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("orderId");
			description.addProperty("itemId");
			description.addProperty("id");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("orderId");
			description.addProperty("itemId");
			description.addProperty("id");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("orderId");
			description.addProperty("itemId");
			description.addProperty("id");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<OrderDetailId> orderDetailIdList = Context.getService(PharmacyService.class).getAll(OrderDetailId.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<OrderDetailId>(context, orderDetailIdList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Integer value = Integer.parseInt(context.getParameter("orderId"));
		List<OrderDetailId> orderDetailIdList = Context.getService(PharmacyService.class).getByMasterId(OrderDetailId.class,
		    "orderId", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<OrderDetailId>(context, orderDetailIdList, false);
	}
	
	@Override
	public OrderDetailId getByUniqueId(String uuid) {
		
		return (OrderDetailId) Context.getService(PharmacyService.class).getEntityByUuid(OrderDetailId.class, uuid);
	}
	
	@Override
	public OrderDetailId save(OrderDetailId orderDetailId) {
		return (OrderDetailId) Context.getService(PharmacyService.class).upsert(orderDetailId);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("orderId") == null) {
			throw new ConversionException("Required properties: orderId");
		}
		
		OrderDetailId orderDetailId = this.constructOrderDetailId(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(orderDetailId);
		return ConversionUtil.convertToRepresentation(orderDetailId, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		OrderDetailId orderDetailId = this.constructOrderDetailId(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(orderDetailId);
		return ConversionUtil.convertToRepresentation(orderDetailId, context.getRepresentation());
	}
	
	@Override
	protected void delete(OrderDetailId orderDetailId, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(orderDetailId);
	}
	
	@Override
	public void purge(OrderDetailId orderDetailId, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(orderDetailId);
	}
	
	private OrderDetailId constructOrderDetailId(String uuid, SimpleObject properties) {
		OrderDetailId orderDetailId;
		
		if (uuid != null) {
			orderDetailId = (OrderDetailId) Context.getService(PharmacyService.class).getEntityByUuid(OrderDetailId.class, uuid);
			if (orderDetailId == null) {
				throw new IllegalPropertyException("OrderDetailId not exist");
			}
			
			if (properties.get("orderId") != null) {
				orderDetailId.setOrderId(Integer.valueOf(properties.get("orderLineQuantity").toString()));
			}
			
			if (properties.get("itemId") != null) {
				orderDetailId.setItemId(Integer.valueOf(properties.get("itemSoh").toString()));
			}
			
		} else {
			orderDetailId = new OrderDetailId();
			if (properties.get("orderLineQuantity") == null) {
				throw new IllegalPropertyException("Required parameters: orderLineQuantity");
			}
			orderDetailId.setOrderId(Integer.valueOf(properties.get("orderId").toString()));
			orderDetailId.setItemId(Integer.valueOf(properties.get("itemId").toString()));
		}
		
		return orderDetailId;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/orderDetailId";
	}
	
}
