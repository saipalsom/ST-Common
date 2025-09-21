package org.saipal.common.utility;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

public class STUtils {


	public static Long getUniqueId() {

		Random random = new Random();

		// Generate random numbers A and C in the range [1, 10000]
		int A = random.nextInt(10000) + 1;
		int C = random.nextInt(10000) + 1;

		// Extract a numeric part of UUID as B
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		long B = Long.parseLong(uuid, 16) % 100000000; // Convert hex to decimal and limit its size

		// Generate random index for permutation selection
		int ind = random.nextInt(10);

		// Reorder based on index logic
		String ret;
		if (ind == 0 || ind == 6) { // ABC
			ret = A + "" + B + "" + C;
		} else if (ind == 1 || ind == 7) { // ACB
			ret = A + "" + C + "" + B;
		} else if (ind == 2) { // BAC
			ret = B + "" + A + "" + C;
		} else if (ind == 3) { // BCA
			ret = B + "" + C + "" + A;
		} else if (ind == 4 || ind == 8) { // CAB
			ret = C + "" + A + "" + B;
		} else { // CBA (also for 5 and 9)
			ret = C + "" + B + "" + A;
		}

		return Long.parseLong(ret);
	}



	public static <T> T deepCopy(T object, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			
			String json = mapper.writeValueAsString(object);
			return mapper.readValue(json, type);
		} catch (IOException e) {
			throw new RuntimeException("Deep copy failed", e);
		}
	}
	public static String getEmailDomain(String email) {
		return email.substring(email.indexOf('@') + 1);
	}
	public static String getTopicName(String recepient) {
		return recepient.substring(recepient.indexOf("@") + 1).replace(".", "");
	}
	public static boolean equals(Long val1, Long val2) {
		if (val1 == null || val2 == null) {
			return false;
		}
		return val1.longValue() == val2.longValue();
	}
}
