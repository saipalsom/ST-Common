package org.saipal.common.service.impl.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.saipal.common.exception.STRuntimeException;
import org.saipal.common.service.STFileService;
import org.springframework.core.io.Resource;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
public class STGoogleCloudService implements STFileService {

	private final Storage storage;

	private String bucketName;

	public STGoogleCloudService(Resource credentialsPath, String bucketName, String orgKey) {

		try (InputStream credentialsStream = credentialsPath.getInputStream();) {
			this.storage = StorageOptions.newBuilder()
					.setCredentials(ServiceAccountCredentials.fromStream(credentialsStream)).build().getService();
		} catch (IOException e) {
			throw new STRuntimeException(
					"cannot initialize google cloud service for " + orgKey + " from path " + credentialsPath);
		}
		this.bucketName = bucketName;
	}

	@Override
	public boolean uploadFile(byte[] documentByteArr, String fileName) throws Exception {
		try {
			BlobId blobId = BlobId.of(bucketName, fileName);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

			storage.create(blobInfo, documentByteArr);
			return true;
		} catch (Exception e) {
			throw new Exception("Upload failed: " + e.getMessage(), e);
		}
	}

	@Override
	public byte[] downloadFile(String filePath) throws Exception {
		try {
			Blob blob = storage.get(BlobId.of(bucketName, filePath));
			if (blob == null || !blob.exists()) {
				throw new Exception("File not found: " + filePath);
			}
			return blob.getContent();
		} catch (Exception e) {
			throw new Exception("Download failed: " + e.getMessage(), e);
		}
	}

	@Override
	public void removeFile(String filePath) throws Exception {
		filePath = formatFolderName(filePath);
		Blob blob = storage.get(BlobId.of(bucketName, filePath));
		storage.delete(blob.getBlobId());
	}

	@Override
	public WMSFileUploadPlatform getPlatformType() {
		return WMSFileUploadPlatform.GOOGLE_CLOUD;
	}

	@Override
	public void mkdir(String folderPath) throws Exception {
		folderPath = formatFolderName(folderPath);
		BlobInfo folder = BlobInfo.newBuilder(bucketName, folderPath).build();
		storage.create(folder, new byte[0]);

	}

	@Override
	public void renameFolder(String oldPath, String newPath) throws Exception {
		oldPath = formatFolderName(oldPath);
		newPath = formatFolderName(newPath);

		Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(oldPath));
		for (Blob blob : blobs.iterateAll()) {
			String newName = newPath + blob.getName().substring(oldPath.length());
			storage.copy(Storage.CopyRequest.of(blob.getBlobId(), BlobId.of(bucketName, newName)));
			storage.delete(blob.getBlobId());
		}

	}

	@Override
	public void move(String sourcePath, String destinationPath) throws Exception {
		BlobId source = BlobId.of(bucketName, sourcePath);
		BlobId destination = BlobId.of(bucketName, destinationPath);

		storage.copy(Storage.CopyRequest.of(source, destination));
		storage.delete(source);

	}

	@Override
	public List<String> list(String folderPath) throws Exception {
		if (!folderPath.endsWith("/")) {
			folderPath += "/";
		}

		Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath),
				Storage.BlobListOption.currentDirectory()); // Prevent recursive listing

		return blobs.streamValues().map(Blob::getName).collect(Collectors.toList());

	}

	@Override
	public void deleteFolder(String folderPath) throws Exception {
		folderPath = formatFolderName(folderPath);
		Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath));
		for (Blob blob : blobs.iterateAll()) {
			storage.delete(blob.getBlobId());
		}
	}

	@Override
	public void copy(String sourceFilePath, String targetFilePath) throws Exception {
		BlobId sourceBlobId = BlobId.of(bucketName, sourceFilePath);
		BlobId targetBlobId = BlobId.of(bucketName, targetFilePath);

		storage.copy(Storage.CopyRequest.of(sourceBlobId, targetBlobId));

	}

	private String formatFolderName(String folderName) {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}
		return folderName;
	}

}
