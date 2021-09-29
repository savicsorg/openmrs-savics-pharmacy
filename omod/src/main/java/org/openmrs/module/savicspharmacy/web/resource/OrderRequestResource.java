package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
			description.addProperty("name");
			description.addProperty("dateApprobation");
			description.addProperty("date");
			description.addProperty("person");
			description.addProperty("supplier");
			description.addProperty("orderDetails");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("id");
			description.addProperty("uuid");
			description.addProperty("date");
			description.addProperty("amount");
			description.addProperty("name");
			description.addProperty("dateApprobation");
			description.addProperty("date");
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
			description.addProperty("name");
			description.addProperty("dateApprobation");
			description.addProperty("date");
			description.addProperty("person");
			description.addProperty("supplier");
			description.addProperty("orderDetails");
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
		try {
			PharmacyOrder order = this.constructOrder(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(order);
			
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByid(PharmacyOrder.class, "id",
			    order.getId());
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(order.getOrderDetails());
			for (int i = 0; i < list.size(); i++) {
				OrderDetail o = new OrderDetail();
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id",
				    Integer.valueOf(list.get(i).get("item").toString()));
				o.setOrderLineQuantity(Integer.valueOf(list.get(i).get("orderLineQuantity").toString()));
				o.setItemSoh(Integer.valueOf(list.get(i).get("itemSoh").toString()));
				o.setItemAmc(Integer.valueOf(list.get(i).get("itemAmc").toString()));
				o.setOrderLineAmount(Double.valueOf(list.get(i).get("orderLineAmount").toString()));
				OrderDetailId pk = new OrderDetailId(item.getId(), order.getId());
				o.setId(pk.hashCode());
				o.setPk(pk);
				o.setItem(item);
				o.setPharmacyOrder(order);
				Context.getService(PharmacyService.class).upsert(o);
			}
			
			return ConversionUtil.convertToRepresentation(order, context.getRepresentation());
		}
		catch (ParseException e) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		PharmacyOrder order;
		try {
			order = this.constructOrder(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(order);
			
			List<OrderDetail> detailList = Context.getService(PharmacyService.class).getByMasterId(OrderDetail.class,
			    "reception.id", order.getId(), 1000, 0);
			for (int i = 0; i < detailList.size(); i++) {
				OrderDetail o = detailList.get(i);
				Context.getService(PharmacyService.class).delete(o);
			}
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByid(PharmacyOrder.class, "id",
			    order.getId());
			List<LinkedHashMap> list = new ArrayList<LinkedHashMap>(order.getOrderDetails());
			for (int i = 0; i < list.size(); i++) {
				OrderDetail o = new OrderDetail();
				Item item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id",
				    Integer.valueOf(list.get(i).get("item").toString()));
				o.setOrderLineQuantity(Integer.valueOf(list.get(i).get("orderLineQuantity").toString()));
				o.setItemSoh(Integer.valueOf(list.get(i).get("itemSoh").toString()));
				o.setItemAmc(Integer.valueOf(list.get(i).get("itemAmc").toString()));
				o.setOrderLineAmount(Double.valueOf(list.get(i).get("orderLineAmount").toString()));
				OrderDetailId pk = new OrderDetailId(item.getId(), order.getId());
				o.setId(pk.hashCode());
				o.setPk(pk);
				o.setItem(item);
				o.setPharmacyOrder(order);
				Context.getService(PharmacyService.class).upsert(o);
			}
			
			return ConversionUtil.convertToRepresentation(order, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(OrderRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
		
	}
	
	@Override
	protected void delete(PharmacyOrder order, String reason, RequestContext context) throws ResponseException {
		List<OrderDetail> orderDetailList = Context.getService(PharmacyService.class).getByMasterId(OrderDetail.class,
		    "pharmacyOrder.id", order.getId(), 1000, 0);
		for (int i = 0; i < orderDetailList.size(); i++) {
			OrderDetail o = orderDetailList.get(i);
			Context.getService(PharmacyService.class).delete(o);
		}
		Context.getService(PharmacyService.class).delete(order);
	}
	
	@Override
	public void purge(PharmacyOrder order, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(order);
	}
	
	private PharmacyOrder constructOrder(String uuid, SimpleObject properties) throws ParseException {
		PharmacyOrder order;
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Supplier supplier = null;
		if (properties.get("supplier") != null) {
			Integer supplierId = properties.get("supplier");
			supplier = (Supplier) Context.getService(PharmacyService.class).getEntityByid(Supplier.class, "id", supplierId);
		}
		
		if (uuid != null) {
			order = (PharmacyOrder) Context.getService(PharmacyService.class).getEntityByUuid(PharmacyOrder.class, uuid);
			if (order == null) {
				throw new IllegalPropertyException("Orders not exist");
			}
			
			if (properties.get("name") != null) {
				order.setName((String) properties.get("name"));
			}
			
			if (properties.get("date") != null) {
				order.setDate(simpleDateFormat.parse(properties.get("date").toString()));
			}
			
			if (properties.get("dateApprobation") != null) {
				order.setDateApprobation(new Date());
			}
			
			if (properties.get("datePharmacyOrder") != null) {
				order.setDate(simpleDateFormat.parse(properties.get("datePharmacyOrder").toString()));
			}
			
			if (properties.get("amount") != null) {
				order.setAmount(Double.valueOf(properties.get("amount").toString()));
			}
			
			if (properties.get("orderDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("orderDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				order.setOrderDetails(set);
			}
			
		} else {
			order = new PharmacyOrder();
			order.setPerson(Context.getUserContext().getAuthenticatedUser().getPerson());
			order.setDate(new Date());
			order.setName(properties.get("name").toString());
			if (properties.get("orderDetails") != null) {
				List<LinkedHashMap> list = (ArrayList<LinkedHashMap>) properties.get("orderDetails");
				Set<LinkedHashMap> set = new HashSet<LinkedHashMap>(list);
				order.setOrderDetails(set);
			}
			if (properties.get("amount") != null)
				order.setAmount(Double.valueOf(properties.get("amount").toString()));
			else
				order.setAmount(0.0);
			order.setSupplier(supplier);
		}
		
		return order;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/order";
	}
	
}
