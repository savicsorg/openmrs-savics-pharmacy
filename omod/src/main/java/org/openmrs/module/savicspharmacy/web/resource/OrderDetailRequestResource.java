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
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.OrderDetail;
import org.openmrs.module.savicspharmacy.api.entity.OrderDetailId;
import org.openmrs.module.savicspharmacy.api.entity.PharmacyOrder;
import org.openmrs.module.savicspharmacy.api.entity.Supplier;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/orderDetail", supportedClass = OrderDetail.class, supportedOpenmrsVersions = { "2.*.*" })
public class OrderDetailRequestResource extends DelegatingCrudResource<OrderDetail> {
	
	@Override
	public OrderDetail newDelegate() {
		return new OrderDetail();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("pharmacyOrder");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemSoh");
			description.addProperty("itemAmc");
			description.addProperty("orderLineAmount");
			description.addProperty("uuid");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("pharmacyOrder");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemSoh");
			description.addProperty("itemAmc");
			description.addProperty("orderLineAmount");
			description.addProperty("uuid");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("pharmacyOrder");
			description.addProperty("id");
			description.addProperty("orderLineQuantity");
			description.addProperty("itemSoh");
			description.addProperty("itemAmc");
			description.addProperty("orderLineAmount");
			description.addProperty("uuid");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<OrderDetail> orderDetailList = Context.getService(PharmacyService.class).getAll(OrderDetail.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<OrderDetail>(context, orderDetailList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Integer value = Integer.parseInt(context.getParameter("orderId"));
		List<OrderDetail> orderDetailList = Context.getService(PharmacyService.class).getByMasterId(OrderDetail.class,
		    "pharmacyOrder.id", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<OrderDetail>(context, orderDetailList, false);
	}
	
	@Override
	public OrderDetail getByUniqueId(String uuid) {
		
		return (OrderDetail) Context.getService(PharmacyService.class).getEntityByUuid(OrderDetail.class, uuid);
	}
	
	@Override
	public OrderDetail save(OrderDetail orderDetail) {
		return (OrderDetail) Context.getService(PharmacyService.class).upsert(orderDetail);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		if (propertiesToCreate.get("orderLineQuantity") == null) {
			throw new ConversionException("Required properties: orderLineQuantity");
		}
		
		OrderDetail orderDetail = this.constructOrderDetail(null, propertiesToCreate);
		Context.getService(PharmacyService.class).upsert(orderDetail);
		return ConversionUtil.convertToRepresentation(orderDetail, context.getRepresentation());
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		OrderDetail orderDetail = this.constructOrderDetail(uuid, propertiesToUpdate);
		Context.getService(PharmacyService.class).upsert(orderDetail);
		return ConversionUtil.convertToRepresentation(orderDetail, context.getRepresentation());
	}
	
	@Override
	protected void delete(OrderDetail orderDetail, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(orderDetail);
	}
	
	@Override
	public void purge(OrderDetail orderDetail, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(orderDetail);
	}
	
	private OrderDetail constructOrderDetail(String uuid, SimpleObject properties) {
		OrderDetail orderDetail;
		
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
		}
		
		PharmacyOrder order = null;
		if (properties.get("pharmacyOrder") != null) {
			Integer orderId = properties.get("pharmacyOrder");
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByid(PharmacyOrder.class, "id",
			    orderId);
		}
		
		if (uuid != null) {
			orderDetail = (OrderDetail) Context.getService(PharmacyService.class).getEntityByUuid(OrderDetail.class, uuid);
			if (orderDetail == null) {
				throw new IllegalPropertyException("OrderDetail not exist");
			}
			
			if (properties.get("orderLineQuantity") != null) {
				orderDetail.setOrderLineQuantity(Integer.valueOf(properties.get("orderLineQuantity").toString()));
			}
			
			if (properties.get("itemSoh") != null) {
				orderDetail.setItemSoh(Integer.valueOf(properties.get("itemSoh").toString()));
			}
			
			if (properties.get("itemAmc") != null) {
				orderDetail.setItemAmc(Integer.valueOf(properties.get("itemAmc").toString()));
			}
			
			if (properties.get("orderLineAmount") != null) {
				orderDetail.setOrderLineAmount(Double.valueOf(properties.get("orderLineAmount").toString()));
			}
			
		} else {
			orderDetail = new OrderDetail();
			if (properties.get("orderLineQuantity") == null) {
				throw new IllegalPropertyException("Required parameters: orderLineQuantity");
			}
			orderDetail.setOrderLineQuantity(Integer.valueOf(properties.get("orderLineQuantity").toString()));
			orderDetail.setItemSoh(Integer.valueOf(properties.get("itemSoh").toString()));
			orderDetail.setItemAmc(Integer.valueOf(properties.get("itemAmc").toString()));
			orderDetail.setOrderLineAmount(Double.valueOf(properties.get("orderLineAmount").toString()));
                        OrderDetailId pk = new OrderDetailId(item.getId(), order.getId());
                        orderDetail.setId(pk.hashCode());
			orderDetail.setPk(pk);
		}
		
		return orderDetail;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/orderDetail";
	}
	
}
