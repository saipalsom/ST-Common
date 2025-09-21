package org.saipal.common.utility;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.saipal.common.entity.DataMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class ToStringUtils {

	private ToStringUtils() {
		// nothing to do here
	}

	/**
	 * Make toString with reflection technique for all fields including parent.
	 *
	 * @return toString in format as below.
	 *
	 *         <pre>
	 * Person[name=John Doe,age=33,smoker=false]
	 *         </pre>
	 */
	public static String toStringAllFields(Object object) {
		if (object == null) {
			return null;
		}

		ToStringBuilder toStringBuilder = new WMSExcludeFieldToStringBuilder(object, ToStringStyle.SHORT_PREFIX_STYLE);
		return toStringBuilder.toString();
	}

	/**
	 * Make toString with reflection technique for all fields including parent.
	 *
	 * @param excludeNullValueField if true, null value fields are excluded
	 * @return toString in format as below.
	 *
	 *         <pre>
	 * Person[name=John Doe,age=33,smoker=false]
	 *         </pre>
	 */
	public static String toStringAllFields(Object object, boolean excludeNullValueField) {
		if (object == null) {
			return null;
		}

		ToStringBuilder toStringBuilder = new WMSExcludeFieldToStringBuilder(object, ToStringStyle.SHORT_PREFIX_STYLE,
				excludeNullValueField);
		return toStringBuilder.toString();
	}

	/**
	 * Make toString with only class defined fields.
	 *
	 * @return toString in format as below.
	 *
	 *         <pre>
	 * Person[name=John Doe,age=33,smoker=false]
	 *         </pre>
	 */
	public static String toStringOnlyClassFields(Object object) {
		if (object == null) {
			return null;
		}

		ReflectionToStringBuilder toStringBuilder = new WMSExcludeFieldToStringBuilder(object,
				ToStringStyle.SHORT_PREFIX_STYLE);
		toStringBuilder.setUpToClass(object.getClass());
		return toStringBuilder.toString();
	}

	/**
	 * Make toString with exclude some fields. (Normally is binary fields)
	 *
	 * @return toString in format as below.
	 *
	 *         <pre>
	 * Person[name=John Doe,age=33,smoker=false]
	 *         </pre>
	 */
	public static final <T> String toStringExcludeFields(T object, String... excludeFieldNames) {
		if (object == null) {
			return null;
		}

		ReflectionToStringBuilder toStringBuilder = new WMSExcludeFieldToStringBuilder(object,
				ToStringStyle.SHORT_PREFIX_STYLE);
		toStringBuilder.setExcludeFieldNames(excludeFieldNames);
		return toStringBuilder.toString();
	}

	private static class WMSExcludeFieldToStringBuilder extends ReflectionToStringBuilder {

		private static final int LENGTH_THRESHOLD = 100;

		boolean excludeNullValueField = false;

		@Override
		protected boolean accept(Field field) {
			return !field.getName().equals("serialVersionUID") && super.accept(field);
		}

		public WMSExcludeFieldToStringBuilder(Object object, ToStringStyle style) {
			super(object, style);
			setAppendStatics(false);
			setAppendTransients(false);
		}

		/**
		 * @param excludeNullValueField if true, null value fields are excluded
		 */
		public WMSExcludeFieldToStringBuilder(Object object, ToStringStyle style, boolean excludeNullValueField) {
			super(object, style);
			setAppendStatics(false);
			setAppendTransients(false);
			this.excludeNullValueField = excludeNullValueField;
		}

		@Override
		protected Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
			Object value = super.getValue(field);
			if (field.getDeclaringClass().getName().contains("serialVersionUID")) {
				return null; // Skip JDK internal fields
			}
			if (value != null) {
				if (field.isAnnotationPresent(ToStringSecure.class)) {
					return "****";
				}

				// Check for byte[].
				if (value.getClass().isAssignableFrom(byte[].class)) {
					byte[] data = (byte[]) value;
					return "byte[" + data.length + "]";
				} else if (value.getClass().isAssignableFrom(Byte[].class)) {
					Byte[] data = (Byte[]) value;
					return "Byte[" + data.length + "]";
				}

				if (value instanceof Object[]) {
					Object[] array = (Object[]) value;

					if (array.length > 20) {
						return array[0].getClass().getName() + "[" + array.length + "]";
					}
				}
				if (value instanceof JsonNode) {
					return summarizeJsonNode((JsonNode) value);
				}

				if (value instanceof List<?>) {
					List<?> list = (List<?>) value;

					if (list.size() > 20) {
						return list.get(0).getClass().getName() + "[" + list.size() + "]";
					}
				}
				if (value instanceof String) {
					final int length = ((String) value).length();
					if (length > LENGTH_THRESHOLD) {
						return "String[" + length + "]";
					}
				}

				if (value instanceof DataMap) {
					try {
						value = getMapValue(value);
					} catch (Exception ex) {
						return value;
					}
				}
			}

			return value;
		}

		private Object getMapValue(Object value) {
			StringBuilder sb = new StringBuilder("DataMap[");
			DataMap map = (DataMap) value;

			for (String key : map.keySet()) {
				final Object entryValue = map.get(key);

				if (entryValue != null) {
					Class<? extends Object> clazz = entryValue.getClass();

					sb.append(key).append(" = ");

					if (clazz.isAssignableFrom(byte[].class)) {
						sb.append("byte[" + ((byte[]) entryValue).length + "]");
					} else if (clazz.isAssignableFrom(Byte[].class)) {
						sb.append("byte[" + ((Byte[]) entryValue).length + "]");
					} else if (entryValue instanceof Object[]) {
						Object[] array = (Object[]) entryValue;

						if (array.length > 20) {
							sb.append(array[0].getClass().getName() + "[" + array.length + "]");
						} else {
							sb.append(entryValue);
						}
					} else if (entryValue instanceof List<?>) {
						List<?> list = (List<?>) entryValue;

						if (list.size() > 20) {
							sb.append(list.get(0).getClass().getName() + "[" + list.size() + "]");
						} else {
							sb.append(entryValue);
						}
					} else if (entryValue instanceof String) {
						String val = (String) entryValue;

						if (val.length() > LENGTH_THRESHOLD) {
							sb.append("String[" + val.length() + "]");
						} else {
							sb.append(val);
						}
					} else {
						sb.append(entryValue);
					}

					sb.append(", ");
				}
			}

			sb.append("] ");
			value = sb.toString();
			return value;
		}

		@Override
		protected void appendFieldsIn(Class<?> clazz) {
			if (clazz.isArray()) {
				reflectionAppendArray(getObject());
				return;
			}

			List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
			List<Field> newFields = new ArrayList<>();
			for (Field field : fields) {
				if (!field.getDeclaringClass().getName().startsWith("java")) {
					newFields.add(field);
				}
			}
			AccessibleObject.setAccessible(newFields.toArray(new Field[0]), true);
			for (Field field : fields) {
				String fieldName = field.getName();

				if (accept(field)) {
					try {
						// Warning: Field.get(Object) creates wrappers objects for primitive types.
						Object fieldValue = getValue(field);

						if (fieldValue != null || !excludeNullValueField) {
							this.append(fieldName, fieldValue);
						}
					} catch (IllegalAccessException e) {
						// this can't happen. Would get a Security exception instead throw a runtime
						// exception in case
						// the impossible happens.
//						throw new InternalError("Unexpected IllegalAccessException: " + e.getMessage());
//						continue;
					}
				}
			}
		}

	}

	public static String mapToString(Map<String, Object> map) {
		if (map != null && !map.isEmpty()) {
			StringBuilder sb = new StringBuilder("[");
			for (String key : map.keySet()) {

				final Object entryValue = map.get(key);

				if (entryValue != null) {
					Class<? extends Object> clazz = entryValue.getClass();

					sb.append(key).append(" = ");

					if (clazz.isAssignableFrom(byte[].class)) {
						sb.append("byte[" + ((byte[]) entryValue).length + "]");
					} else if (clazz.isAssignableFrom(Byte[].class)) {
						sb.append("byte[" + ((Byte[]) entryValue).length + "]");
					} else if (entryValue instanceof Object[]) {
						Object[] array = (Object[]) entryValue;

						if (array.length > 20) {
							sb.append(array[0].getClass().getName() + "[" + array.length + "]");
						} else {
							sb.append(entryValue);
						}
					} else if (entryValue instanceof List<?>) {
						List<?> list = (List<?>) entryValue;

						if (list.size() > 20) {
							sb.append(list.get(0).getClass().getName() + "[" + list.size() + "]");
						} else {
							sb.append(entryValue);
						}
					} else if (entryValue instanceof String) {
						String val = (String) entryValue;

						if (val.length() > 100) {
							sb.append("String[" + val.length() + "]");
						} else {
							sb.append(val);
						}
					} else {
						sb.append(entryValue);
					}

					sb.append(", ");
				}
			}

			sb.append("] ");
			return sb.toString();
		} else {
			return null;
		}
	}

	private static String summarizeJsonNode(JsonNode node) {
		int LENGTH_THRESHOLD = 100;
		if (node.isTextual()) {
			String text = node.asText();
			return text.length() > LENGTH_THRESHOLD ? "String[" + text.length() + "]" : text;
		}

		if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			StringBuilder sb = new StringBuilder("Array[").append(arrayNode.size()).append("]: [");

			int count = 0;
			for (JsonNode item : arrayNode) {
				if (count++ >= 5) {
					sb.append("..., ");
					break;
				}
				sb.append(summarizeJsonNode(item)).append(", ");
			}

			// Trim trailing comma if present
			if (sb.length() >= 2) {
				sb.setLength(sb.length() - 2);
			}

			sb.append("]");
			return sb.toString();
		}

		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;
			StringBuilder sb = new StringBuilder("{");
			int count = 0;
			for (Map.Entry<String, JsonNode> entry : objectNode.properties()) {
				if (++count > 5) {
					sb.append("..., ");
					break;
				}
				String key = entry.getKey();
				JsonNode val = entry.getValue();
				sb.append(key).append("=");
				sb.append(summarizeJsonNode(val));
				sb.append(", ");
			}
			if (sb.length() >= 2) {
				sb.setLength(sb.length() - 2);
			}
			sb.append("}");
			return sb.toString();
		}

		if (node.isNumber() || node.isBoolean()) {
			return node.asText();
		}

		// fallback for other node types
		String raw = node.toString();
		return raw.length() > LENGTH_THRESHOLD ? raw.substring(0, LENGTH_THRESHOLD) + "...[truncated]" : raw;
	}

}
