package org.saipal.common.utility;

import java.time.Instant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class STJsonUtils {

	public static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(Instant.class, new CustomInstantSerializer());
		module.addDeserializer(Instant.class, new CustomInstantDeserializer());
		mapper.registerModule(module);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	public static String convertObjectToJsonString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}

	public static String convertObjectToJsonStringPrettyPrint(Object object) throws JsonProcessingException {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
	}

	public static <T> T convertJsonToObject(String json, Class<T> classType)
			throws JsonMappingException, JsonProcessingException {
		return mapper.readValue(json, classType);
	}

	public static <T> T convertJsonByteArrToObject(byte[] json, Class<T> classType)
			throws JsonMappingException, JsonProcessingException {
		String jsonData = new String(json);
		return mapper.readValue(jsonData, classType);
	}

	public static byte[] convertObjectToJsonByteArr(Object object) throws JsonProcessingException {
		String data = convertObjectToJsonString(object);
		if (STStringUtils.isEmpty(data)) {
			return null;
		}
		return data.getBytes();
	}

	public static JsonNode createNewObjectNode() {
		return mapper.createObjectNode();
	}

	public static JsonNode createNewArrayNode() {
		return mapper.createArrayNode();
	}

}
