package org.saipal.common.utility;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

public class STStringUtils {
	public static String trimAndToLower(String value) {
		return value == null ? null : value.trim().toLowerCase();
	}

	public static boolean strEquals(String str1, String str2) {
		if (isEmpty(str1) || isEmpty(str2)) {
			return false;
		}
		return str1.equalsIgnoreCase(str2);
	}

	public static String trimAndToLower(Object value) {
		return value == null || !(value instanceof String) ? null : ((String) value).trim().toLowerCase();
	}

	public static String encodeBase64(String text) {
		if (isEmpty(text)) {
			return null;
		}
		return Base64.getEncoder().encodeToString(text.getBytes());
	}

	public static String decodeBase64(String text) {
		if (isEmpty(text)) {
			return null;
		}
		return new String(Base64.getDecoder().decode(text));

	}

	public static boolean isEmpty(String str) {
		return StringUtils.isBlank(str);
	}

}
