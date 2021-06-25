/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.api.service.impl;

import java.io.Serializable;
import java.util.List;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.savicspharmacy.api.dao.PharmacyDao;
import org.openmrs.module.savicspharmacy.api.service.PharmacyService;

public class PharmacyServiceImpl<T extends Serializable> extends BaseOpenmrsService implements PharmacyService {
	
	private PharmacyDao dao;
	
	private LocationService locationService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 * 
	 * @param dao
	 */
	public void setDao(PharmacyDao dao) {
		this.dao = dao;
	}
	
	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
	
	@Override
	public List getAll(Class t) {
		List entityList = this.dao.getAll(t);
		return entityList;
	}
	
	@Override
	public List getAll(Class t, Integer limit, Integer offset) {
		List entityList = this.dao.getAll(t, limit, offset);
		
		return entityList;
		
	}
	
	@Override
	public List doSearch(Class t, String key, String value, Integer limit, Integer offset) {
		return this.dao.doSearch(t, key, value, limit, offset);
	}
	
	@Override
	public T getEntity(Class t, Object id) {
		return (T) this.dao.getEntity(t, id);
	}
	
	@Override
	public T getEntityByUuid(Class t, String uuid) {
		return (T) this.dao.getEntityByUuid(t, uuid);
	}
	
	@Override
	public Serializable upsert(Serializable entity) {
		return this.dao.upsert(entity);
	}
	
	@Override
	public void delete(Serializable entity) {
		this.dao.delete(entity);
	}
	
	@Override
	public Serializable getEntityByid(Class t, String idName, Integer id) throws APIException {
		return (T) this.dao.getEntityByid(t, idName, id);
	}
        
        @Override
	public List getByMasterId(Class t, String key, int value, Integer limit, Integer offset) {
		return this.dao.getFromMasterId(t, key, value, limit, offset);
	}
}
