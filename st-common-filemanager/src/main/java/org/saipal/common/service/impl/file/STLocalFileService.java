package org.saipal.common.service.impl.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.saipal.common.exception.STRuntimeException;
import org.saipal.common.service.STFileService;

public class STLocalFileService implements STFileService {

	private final Path rootFolder;

	/**
	 * Constructor to set the root folder.
	 *
	 * @param rootFolderPath the base directory for all file operations (e.g.,
	 *                       "storage/")
	 */
	public STLocalFileService(String rootFolderPath) {
		this.rootFolder = Paths.get(rootFolderPath).toAbsolutePath();
	}

	/**
	 * Uploads a file under the root folder.
	 *
	 * @param fileBytes    the content of the file as a byte array
	 * @param relativePath the file path relative to the root (e.g.,
	 *                     "docs/file.txt")
	 * @return true if upload is successful, false otherwise
	 */
	@Override
	public boolean uploadFile(byte[] fileBytes, String relativePath) throws Exception {
		Path fullPath = rootFolder.resolve(relativePath).normalize();
		Files.createDirectories(fullPath.getParent());
		Files.write(fullPath, fileBytes);
		return true;
	}

	/**
	 * Downloads a file from the root folder.
	 *
	 * @param relativePath the file path relative to the root (e.g.,
	 *                     "docs/file.txt")
	 * @return the file content as a byte array, or null if error occurs
	 */
	@Override
	public byte[] downloadFile(String filePath) throws Exception {
		Path fullPath = rootFolder.resolve(filePath).normalize();
		return Files.readAllBytes(fullPath);
	}

	@Override
	public void removeFile(String filePath) throws Exception {
		Path fullPath = rootFolder.resolve(filePath).normalize();
		Files.delete(fullPath);
	}

	@Override
	public WMSFileUploadPlatform getPlatformType() {
		// TODO Auto-generated method stub
		return WMSFileUploadPlatform.LOCAL;
	}

	@Override
	public void mkdir(String folderPath) throws Exception {
		Path path = rootFolder.resolve(folderPath);
		Files.createDirectories(path);

	}

	@Override
	public void renameFolder(String oldPath, String newPath) throws Exception {
		Path source = rootFolder.resolve(oldPath);
		Path target = rootFolder.resolve(newPath);
		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

	}

	@Override
	public void move(String sourcePath, String destinationPath) throws Exception {
		Path source = rootFolder.resolve(sourcePath);
		Path target = rootFolder.resolve(destinationPath);

		Files.createDirectories(target.getParent());
		Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

	}

	@Override
	public List<String> list(String folderPath) throws Exception {
		Path path = rootFolder.resolve(folderPath);

		if (!Files.isDirectory(path)) {
			throw new STRuntimeException("Not a directory: " + path);
		}

		try (Stream<Path> stream = Files.list(path)) {
			return stream.map(p -> p.getFileName().toString()).collect(Collectors.toList());
		}
	}

	@Override
	public void deleteFolder(String folderPath) throws Exception {
		Path dir = rootFolder.resolve(folderPath);

		if (Files.exists(dir)) {
			Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}

	}

	@Override
	public void copy(String sourceFilePath, String targetFilePath) throws Exception {
		Path source = rootFolder.resolve(sourceFilePath);
		Path target = rootFolder.resolve(targetFilePath);

		Files.createDirectories(target.getParent());
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}
}
