package org.openmrs.module.savicspharmacy.web.resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.openmrs.module.savicspharmacy.api.entity.SendingDetail;
import org.openmrs.module.savicspharmacy.api.entity.SendingDetailId;
import org.openmrs.module.savicspharmacy.api.entity.Sending;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;
import org.openmrs.module.savicspharmacy.rest.v1_0.resource.PharmacyRest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

@Resource(name = RestConstants.VERSION_1 + PharmacyRest.PHARMACY_NAMESPACE + "/sendingDetail", supportedClass = SendingDetail.class, supportedOpenmrsVersions = { "2.*.*" })
public class SendingDetailRequestResource extends DelegatingCrudResource<SendingDetail> {
	
	@Override
	public SendingDetail newDelegate() {
		return new SendingDetail();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("sending");
			description.addProperty("id");
			description.addProperty("sendingDetailsQuantity");
			description.addProperty("sendingDetailsValue");
			description.addProperty("sendingItemBatch");
			description.addProperty("sendingItemExpiryDate");
			description.addProperty("uuid");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("sending");
			description.addProperty("id");
			description.addProperty("sendingDetailsQuantity");
			description.addProperty("sendingDetailsValue");
			description.addProperty("sendingItemBatch");
			description.addProperty("sendingItemExpiryDate");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("pk");
			description.addProperty("item");
			description.addProperty("sending");
			description.addProperty("id");
			description.addProperty("sendingDetailsQuantity");
			description.addProperty("sendingDetailsValue");
			description.addProperty("sendingItemBatch");
			description.addProperty("sendingItemExpiryDate");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getAll(SendingDetail.class,
		    context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<SendingDetail>(context, sendingDetailList, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Integer value = Integer.parseInt(context.getParameter("sendingId"));
		List<SendingDetail> sendingDetailList = Context.getService(PharmacyService.class).getByMasterId(SendingDetail.class,
		    "sending.id", value, context.getLimit(), context.getStartIndex());
		return new AlreadyPaged<SendingDetail>(context, sendingDetailList, false);
	}
	
	@Override
	public SendingDetail getByUniqueId(String uuid) {
		
		return (SendingDetail) Context.getService(PharmacyService.class).getEntityByUuid(SendingDetail.class, uuid);
	}
	
	@Override
	public SendingDetail save(SendingDetail sendingDetail) {
		return (SendingDetail) Context.getService(PharmacyService.class).upsert(sendingDetail);
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		try {
			if (propertiesToCreate.get("sendingDetailsQuantity") == null) {
				throw new ConversionException("Required properties: sendingDetailsQuantity");
			}
			
			SendingDetail sendingDetail = this.constructSendingDetail(null, propertiesToCreate);
			Context.getService(PharmacyService.class).upsert(sendingDetail);
			return ConversionUtil.convertToRepresentation(sendingDetail, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(SendingDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		try {
			SendingDetail sendingDetail = this.constructSendingDetail(uuid, propertiesToUpdate);
			Context.getService(PharmacyService.class).upsert(sendingDetail);
			return ConversionUtil.convertToRepresentation(sendingDetail, context.getRepresentation());
		}
		catch (ParseException ex) {
			Logger.getLogger(SendingDetailRequestResource.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}
	
	@Override
	protected void delete(SendingDetail sendingDetail, String reason, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(sendingDetail);
	}
	
	@Override
	public void purge(SendingDetail sendingDetail, RequestContext context) throws ResponseException {
		Context.getService(PharmacyService.class).delete(sendingDetail);
	}
	
	private SendingDetail constructSendingDetail(String uuid, SimpleObject properties) throws ParseException {
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SendingDetail sendingDetail;
		
		Item item = null;
		if (properties.get("item") != null) {
			Integer itemId = properties.get("item");
			item = (Item) Context.getService(PharmacyService.class).getEntityByid(Item.class, "id", itemId);
		}
		
		//		if (properties.get("sendingItemBatch") != null && uuid == null && properties.get("sendingDetailsQuantity") != null) {
		//			ItemsLine line = (ItemsLine) Context.getService(PharmacyService.class).getEntityByAttributes(ItemsLine.class,
		//			    new String[] { "itemBatch" }, new Object[] { properties.get("sendingItemBatch").toString() });
		//			line.setItemVirtualstock(line.getItemVirtualstock()
		//			        + Integer.valueOf(properties.get("sendingDetailsQuantity").toString()));
		//			Context.getService(PharmacyService.class).upsert(line);
		//		}
		
		Sending sending = null;
		if (properties.get("sending") != null) {
			Integer sendingId = properties.get("sending");
			sending = (Sending) Context.getService(PharmacyService.class).getEntityByid(Sending.class, "id", sendingId);
		}
		
		if (uuid != null) {
			sendingDetail = (SendingDetail) Context.getService(PharmacyService.class).getEntityByUuid(SendingDetail.class,
			    uuid);
			if (sendingDetail == null) {
				throw new IllegalPropertyException("SendingDetail not exist");
			}
			
			if (properties.get("sendingDetailsQuantity") != null) {
				sendingDetail
				        .setSendingDetailsQuantity(Integer.valueOf(properties.get("sendingDetailsQuantity").toString()));
			}
			
			if (properties.get("sendingDetailsValue") != null) {
				sendingDetail.setSendingDetailsValue(Integer.valueOf(properties.get("sendingDetailsValue").toString()));
			}
			
			if (properties.get("sendingItemBatch") != null) {
				sendingDetail.setSendingItemBatch(properties.get("sendingItemBatch").toString());
			}
			
			if (properties.get("sendingItemExpiryDate") != null) {
				sendingDetail.setSendingItemExpiryDate(simpleDateFormat.parse(properties.get("sendingItemExpiryDate")
				        .toString()));
			}
			
		} else {
			sendingDetail = new SendingDetail();
			if (properties.get("sendingDetailsQuantity") == null) {
				throw new IllegalPropertyException("Required parameters: sendingDetailsQuantity");
			}
			if (properties.get("sendingDetailsQuantity") != null) {
				sendingDetail
				        .setSendingDetailsQuantity(Integer.valueOf(properties.get("sendingDetailsQuantity").toString()));
			}
			
			if (properties.get("sendingDetailsValue") != null) {
				sendingDetail.setSendingDetailsValue(Integer.valueOf(properties.get("sendingDetailsValue").toString()));
			}
			
			if (properties.get("sendingItemBatch") != null) {
				sendingDetail.setSendingItemBatch(properties.get("sendingItemBatch").toString());
			}
			
			if (properties.get("sendingItemExpiryDate") != null) {
				sendingDetail.setSendingItemExpiryDate(simpleDateFormat.parse(properties.get("sendingItemExpiryDate")
				        .toString()));
			}
			
			SendingDetailId pk = new SendingDetailId(item.getId(), sending.getId());
			sendingDetail.setId(pk.hashCode());
			sendingDetail.setPk(pk);
			sendingDetail.setItem(item);
			sendingDetail.setSending(sending);
		}
		
		return sendingDetail;
	}
	
	@Override
	public String getUri(Object instance) {
		return RestConstants.URI_PREFIX + "/sendingDetail";
	}
	
}
