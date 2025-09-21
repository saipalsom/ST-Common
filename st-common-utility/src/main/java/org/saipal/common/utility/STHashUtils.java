package org.saipal.common.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class STHashUtils {

	public static final String ALGORITHM_SHA256 = "SHA-256";
	
	public static String getHash(String data) throws Exception {
		return getHash(data, ALGORITHM_SHA256);
	}

	public static String getHash(String data, String algorithm) throws Exception {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hash);
	}
}
