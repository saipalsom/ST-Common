package org.saipal.common.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringExclude;
import org.saipal.common.utility.STStringUtils;
import org.saipal.common.utility.ToStringUtils;

import lombok.extern.slf4j.Slf4j;



@SuppressWarnings("serial")
@Slf4j
public class DataMap extends HashMap<String, Object> {
	@ToStringExclude
	private final boolean caseSensitiveKey;

	public DataMap(boolean caseSensitiveKey) {
		super();
		this.caseSensitiveKey = caseSensitiveKey;
	}

	public DataMap() {
		super();
		this.caseSensitiveKey = false;
	}

	@Override
	public Object get(Object key) {
		if (!caseSensitiveKey) {
			key = STStringUtils.trimAndToLower(key);
		}
		return super.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		if (!caseSensitiveKey) {
			key = STStringUtils.trimAndToLower(key);
		}
		return super.containsKey(key);
	}

	@Override
	public Object put(String key, Object value) {
		if (!caseSensitiveKey) {
			key = STStringUtils.trimAndToLower(key);
		}
		return super.put(key, value);
	}

	public <T> T getAs(String key, Class<T> type) {
		Object value = get(key);
		if (value != null) {

		if (type.isInstance(value)) {
			return type.cast(value);
		} // Generic number coercion
		if (value instanceof Number && Number.class.isAssignableFrom(type)) {
			T result = coerceNumber(value, type);
			if (result != null) {
				return result;
			}
		}
		 if (value instanceof String && Number.class.isAssignableFrom(type)) {
	            try {
	                String str = ((String) value).trim();
	                if (!str.isEmpty()) {
	                    Number parsed = parseStringToNumber(str, type);
	                    if (parsed != null) {
	                        return type.cast(parsed);
	                    }
	                }
	            } catch (Exception ignored) {
	                // Ignore parsing failures silently
	            }
	        }
	}
	return null;

}

	private Number parseStringToNumber(String str, Class<?> type) {
	    if (type == Integer.class) {
			return Integer.parseInt(str);
		}
	    if (type == Long.class) {
			return Long.parseLong(str);
		}
	    if (type == Double.class) {
			return Double.parseDouble(str);
		}
	    if (type == Float.class) {
			return Float.parseFloat(str);
		}
	    if (type == Short.class) {
			return Short.parseShort(str);
		}
	    if (type == Byte.class) {
			return Byte.parseByte(str);
		}
	    if (type == BigInteger.class) {
			return new BigInteger(str);
		}
	    if (type == BigDecimal.class) {
			return new BigDecimal(str);
		}
	    return null;
	}

	private static <T> T coerceNumber(Object value, Class<T> targetType) {
		if (!(value instanceof Number)) {
			return null;
		}

		try {
			if (targetType.getConstructor(String.class) != null) {
				return targetType.getConstructor(String.class).newInstance(value.toString());
			}
		} catch (Exception e) {
			// Ignore and fall through
		}

		return null;
	}

	public String getAsString(String key) {
		Object value = get(key);
		return value == null ? null : value.toString();
	}

	public Integer getAsIntNoErr(String key) {
		try {
			return getAsInt(key);
		} catch (Exception e) {
			log.warn("Error while getting value for key [{}]", key, e.getMessage());
			return null;
		}
	}

	public Integer getAsInt(String key) {
		return getAs(key, Integer.class);

	}

	public Long getAsLong(String key) {
		return getAs(key, Long.class);

	}

	public byte[] getAsByteArr(String key) {
		return getAs(key, byte[].class);
	}

	public Boolean getAsBoolean(String key) {
		return getAs(key, Boolean.class);
	}

	public DataMap addMap(Map<String, Object> map) {
		map.forEach((k1, v1) -> {
			put(k1, v1);
		});
		return this;
	}



	@Override
	public String toString() {
		return ToStringUtils.toStringAllFields(this);
	}
}
