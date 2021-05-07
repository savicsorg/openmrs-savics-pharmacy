/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.web.serialization;

/**
 *
 * @author anatoleabe
 */
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class ObjectMapperRepository<E> {
	
	public static ObjectMapper objectMapper = new ObjectMapper();
	
	public String writeValueAsString(List<E> result) throws IOException {
		objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
		return objectMapper.writeValueAsString(result);
	}
}
