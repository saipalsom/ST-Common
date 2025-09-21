package org.saipal.common.utility;

import java.io.IOException;

import org.saipal.common.entity.DataMap;
import org.saipal.common.utility.STJsonUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DataMapConverter implements AttributeConverter<DataMap, String> {

	@Override
	public String convertToDatabaseColumn(DataMap attribute) {
		try {
			return STJsonUtils.convertObjectToJsonString(attribute);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to convert DataMap to JSON", e);
		}
	}

	@Override
	public DataMap convertToEntityAttribute(String dbData) {
		try {
			return STJsonUtils.convertJsonToObject(dbData, DataMap.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to convert JSON to DataMap", e);
		}
	}
}
