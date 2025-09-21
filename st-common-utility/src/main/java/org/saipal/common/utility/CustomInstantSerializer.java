package org.saipal.common.utility;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomInstantSerializer extends JsonSerializer<Instant> {

	@Override
	public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(STDateTimeUtil.formatDateToString(value));
	}
}
