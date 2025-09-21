package org.saipal.common.utility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class STDateTimeUtil {

	private static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss.SSSSSS"; // default
	private static String zone = "Asia/Kathmandu"; // default
	private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
	private static TimeZoneProvider timeZoneProvider = null;

	/**
	 * 
	 * @param format
	 * @param zoneId
	 */
	public static void initialize(String format, String zoneId) {
		if (format != null && !format.isEmpty()) {
			dateTimeFormat = format;
		}
		if (zoneId != null && !zoneId.isEmpty()) {
			zone = zoneId;
		}
		formatter = DateTimeFormatter.ofPattern(dateTimeFormat).withZone(ZoneId.of(zone));
	}

	public static void setTimeZoneProvider(TimeZoneProvider provider) {
		timeZoneProvider = provider;
	}

	public static String formatDate(LocalDateTime date) {
		return date.format(formatter);
	}

	public static String getCurrentDateTimeUtc() {
		return Instant.now().toString();
	}

	public static String getCurrentDayUtc() {
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(UTC_ZONE);
		return dayFormatter.format(Instant.now());
	}

	public static Instant getCurrentDateUtc() {
		return Instant.now();
	}

	public static Instant convertToUtcDate(String dateStr) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormat).withZone(UTC_ZONE);
			return ZonedDateTime.parse(dateStr, formatter).toInstant();
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid date or format: " + e.getMessage(), e);
		}
	}

	public static String formatDateToString(Instant instant) {
		String userTimeZone = null;
		if (timeZoneProvider != null) {
			userTimeZone = timeZoneProvider.getUserTimeZone();
		}

		if (STStringUtils.isEmpty(userTimeZone)) {
			userTimeZone = zone;
		}
		return formatDateToString(instant, dateTimeFormat, userTimeZone);
	}

	public static String formatDateToString(Instant instant, String format) {
		return formatDateToString(instant, format, zone);
	}

	public static String formatDateToString(Instant instant, String format, String zone) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(zone));
		return formatter.format(instant);
	}
}
