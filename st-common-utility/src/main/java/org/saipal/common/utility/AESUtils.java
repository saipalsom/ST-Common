package org.saipal.common.utility;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

	// Generate a random AES key
	public static SecretKey generateAESKey() throws Exception {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128); // AES supports 128, 192, and 256 bits
		return keyGen.generateKey();
	}

	// Encrypt a byte array using AES
	public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
		if (data == null || data.length < 1) {
			return data;
		}
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	// Decrypt a byte array using AES
	public static byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
		if (encryptedData == null || encryptedData.length < 1) {
			return encryptedData;
		}
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(encryptedData);
	}

	// Convert SecretKey to String
	public static String keyToString(SecretKey key) {
		if (key == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	// Convert String back to SecretKey
	public static SecretKey stringToKey(String keyString) {
		if (keyString == null) {
			return null;
		}
		byte[] decodedKey = Base64.getDecoder().decode(keyString);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}
}
