package org.saipal.common.service.impl.file;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.saipal.common.exception.STRuntimeException;
import org.saipal.common.service.STFileService;
import org.springframework.core.io.Resource;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class STGoogleDriveService implements STFileService {

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	private Resource credentialsFile;
	private String rootFolderId;

	public STGoogleDriveService(Resource credentialsFile, String rootFolderId) {
		super();
		this.credentialsFile = credentialsFile;
		this.rootFolderId = rootFolderId;
	}

	private Drive getDriveService() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

		// Load service account credentials using GoogleCredentials
		InputStream credentialsStream = new FileInputStream(credentialsFile.getFile());
		GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream).createScoped(SCOPES);

		return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
				.setApplicationName("Google Drive API Spring Boot").build();
	}

	/**
	 * Uploads a file to Google Drive in the specified folder path.
	 *
	 * @param fileContent The file content as a byte array.
	 * @param filename    The name of the file.
	 * @param mimeType    The MIME type of the file.
	 * @param path        The folder path (e.g., "folder1/folder2").
	 * @return The ID of the uploaded file.
	 */
	@Override
	public boolean uploadFile(byte[] fileContent, String filename) throws Exception {

		Drive driveService = getDriveService();

		// Split the path
		String[] parts = filename.split("/");
		String fileName = parts[parts.length - 1];  // Last part is the file name
		String parentFolderId = rootFolderId;       // Start from root

		// Build folder structure (excluding the file name)
		for (int i = 0; i < parts.length - 1; i++) {
		    String folderName = parts[i];

		    String query = String.format(
		        "name='%s' and mimeType='application/vnd.google-apps.folder' and '%s' in parents and trashed=false",
		        folderName, parentFolderId);

		    FileList result = driveService.files().list()
		        .setQ(query)
		        .setSpaces("drive")
		        .setFields("files(id, name)")
		        .execute();

		    List<File> folders = result.getFiles();
		    if (folders.isEmpty()) {
		        // Create the folder
		        File folderMetadata = new File();
		        folderMetadata.setName(folderName);
		        folderMetadata.setMimeType("application/vnd.google-apps.folder");
		        folderMetadata.setParents(Collections.singletonList(parentFolderId));

		        File folder = driveService.files().create(folderMetadata).setFields("id").execute();
		        parentFolderId = folder.getId();
		    } else {
		        parentFolderId = folders.get(0).getId();
		    }
		}

		// Now upload the file into the last folder
		File fileMetadata = new File();
		fileMetadata.setName(fileName);
		fileMetadata.setParents(Collections.singletonList(parentFolderId));

		String mimeType = new Tika().detect(fileContent);  // Or set manually
		ByteArrayContent mediaContent = new ByteArrayContent(mimeType, fileContent);

		driveService.files()
		    .create(fileMetadata, mediaContent)
		    .setFields("id")
		    .execute();

		return true;

	}



	/**
	 * Downloads a file from Google Drive by its path.
	 *
	 * @param path The file path (e.g., "folder1/folder2/filename.txt").
	 * @return The file content as a byte array.
	 */
	@Override
	public byte[] downloadFile(String path) throws GeneralSecurityException, IOException {
		Drive driveService = getDriveService();
		String fileId = findFileByPath(path);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * Finds a file by its path.
	 *
	 * @param path The file path (e.g., "folder1/folder2/filename.txt").
	 * @return The ID of the file.
	 */
	public String findFileByPath(String path) throws GeneralSecurityException, IOException {
		Drive driveService = getDriveService();

		String[] parts = path.split("/");
		String parentFolderId = rootFolderId; // Start from the root folder

		for (int i = 0; i < parts.length; i++) {
			String mimeTypeFilter = (i == parts.length - 1) ? "" // could be file or folder
					: " and mimeType = 'application/vnd.google-apps.folder'";
			String name = parts[i];
			String query = String.format("name='%s' and '%s' in parents and trashed=false%s", name, parentFolderId,
					mimeTypeFilter);

			FileList result = driveService.files().list().setQ(query).setSpaces("drive")
					.setFields("files(id, name, mimeType)").execute();

			List<File> files = result.getFiles();
			if (files.isEmpty()) {
				throw new IOException("File or folder not found: " + name);
			}

			File file = files.get(0);
			if (i == parts.length - 1) {
				// Last part of the path is the file
				return file.getId();
			} else {
				// Intermediate part is a folder
				parentFolderId = file.getId();
			}
		}

		throw new IOException("File not found: " + path);
	}

	@Override
	public void removeFile(String filePath) throws Exception {
		Drive driveService = getDriveService();
		try {
			String fileId = findFileByPath(filePath);
			if (fileId == null) {
				throw new STRuntimeException("File not found: " + filePath);
			}

			driveService.files().delete(fileId).execute();
			log.debug("Deleted file with ID: " + fileId);
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 404) {
				throw new STRuntimeException("File not found or already deleted: " + filePath, e);
			}
			throw e;
		}

	}

	@Override
	public WMSFileUploadPlatform getPlatformType() {
		// TODO Auto-generated method stub
		return WMSFileUploadPlatform.GOOGLE_DRIVE;
	}

	@Override
	public void mkdir(String folderPath) throws Exception {
		File fileMetadata = new File();
		fileMetadata.setName(folderPath);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		getDriveService().files().create(fileMetadata).setFields("id").execute();

	}

	@Override
	public void renameFolder(String oldPath, String newName) throws Exception {
		String oldPathId = findFileByPath(oldPath);
		File fileMetadata = new File();
		fileMetadata.setName(newName);
		getDriveService().files().update(oldPathId, fileMetadata).execute();

	}

	@Override
	public void move(String sourcePath, String destinationPath) throws Exception {
		String sourceFileId = findFileByPath(sourcePath);
		String destinationFolderId = findFileByPath(destinationPath);
		File file = getDriveService().files().get(sourceFileId).setFields("parents").execute();
		List<String> previousParents = file.getParents();
		String previousParentsStr = String.join(",", previousParents);

		getDriveService().files().update(sourceFileId, null).setAddParents(destinationFolderId)
				.setRemoveParents(previousParentsStr).setFields("id, parents").execute();

	}

	@Override
	public List<String> list(String folderPath) throws Exception {
		String folderId=findFileByPath(folderPath);
		String query = String.format("'%s' in parents and trashed = false", folderId);

		FileList result = getDriveService().files().list().setQ(query).setFields("files(id, name)").execute();

		return result.getFiles().stream().map(File::getName).collect(Collectors.toList());
	}

	@Override
	public void deleteFolder(String folderPath) throws Exception {
		String folderId = findFileByPath(folderPath);
		getDriveService().files().delete(folderId).execute();
	}

	@Override
	public void copy(String sourceFilePath, String targetFilePath) throws Exception {
		String sourceFileId = findFileByPath(sourceFilePath);
		String destinationFileId = findFileByPath(targetFilePath);
		File copiedFile = new File();
		copiedFile.setName("Copied File");
		copiedFile.setParents(List.of(destinationFileId)); // assuming folder ID
		getDriveService().files().copy(sourceFileId, copiedFile).execute();
	}

}