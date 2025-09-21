package org.saipal.common.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.tika.Tika;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class STFileUtils {
	static String fileStorePath = "img";
	private static Tika tika = new Tika();
	public static byte[] decodeBase64(String img) {
		if (img == null) {
			return null;
		}
		return Base64.getDecoder().decode(img);
	}

	public static String encodeBase64(byte[] img) {
		if (img == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(img);
	}


	public static List<byte[]> splitFile(byte[] img, int primaryImagePercentage) {
		int middle = (int) (img.length / 100.0 * primaryImagePercentage);
		byte[] part1 = new byte[middle];
		byte[] part2 = new byte[img.length - middle];

		System.arraycopy(img, 0, part1, 0, middle);
		System.arraycopy(img, middle, part2, 0, img.length - middle);
		return Arrays.asList(part1, part2);
	}

	public static String detectFileType(byte[] img) {
		return tika.detect(img);
	}

	public static String detectFileType(String img) {
		if (img == null) {
			return null;
		}
		return detectFileType(decodeBase64(img));
	}

	public static long getFileSize(byte[] data) {
		return data == null ? 0 : data.length;
	}

	public static long getFileSize(String dataStr) {
		byte[] data = decodeBase64(dataStr);
		return data == null ? 0 : data.length;
	}
	public static boolean storeFile(String storeFileName, byte[] part2) {
		try (FileOutputStream fos = new FileOutputStream(new File(fileStorePath + File.separator + storeFileName))) {
			fos.write(part2);
			return true;
		} catch (IOException e) {
			System.err.println("Error saving file: " + e.getMessage());
			return false;
		}
	}

	public static String createFileName(String fileName, byte[] img) {
		String fileType = detectFileType(img);
		return fileName + "." + fileType.substring(fileType.indexOf("/") + 1);
	}

	public static byte[] retrieveFile(String fileName) {
		Path path = Paths.get(fileStorePath + File.separator + fileName);
		try {
			return Files.readAllBytes(path);

		} catch (IOException e) {
			System.err.println("Error reading file: " + e.getMessage());
			return null;
		}
	}

	public static void saveToFile(String jsonData, String filePath) throws IOException {
		Files.write(Paths.get(filePath), jsonData.getBytes());

	}

	public static String readFromFile(String filePath) throws IOException {
		return Files.readString(Paths.get(filePath));
	}

	public static byte[] readFromFileInBytes(String filePath) throws IOException {
		return Files.readAllBytes(Paths.get(filePath));
	}

	public static boolean removeFile(String filePath) throws IOException {
		try {
			Files.delete(Paths.get(filePath));
		} catch (Exception e) {
			log.error("cannot remove file [{}]", e.getMessage(), e);
			return false;
		}
		return true;
	}
}
