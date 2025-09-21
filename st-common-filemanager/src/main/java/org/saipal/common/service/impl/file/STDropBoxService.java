package org.saipal.common.service.impl.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.saipal.common.service.STFileService;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class STDropBoxService implements STFileService {


	private DbxClientV2 dropBoxClient;
	
	private String basePath;

	public STDropBoxService(DbxClientV2 dropBoxClient, String basePath) {
		super();
		this.dropBoxClient = dropBoxClient;
		this.basePath = basePath;
	}



	@Override
	public boolean uploadFile(byte[] documentByteArr, String filePath) throws Exception {

		filePath = basePath + filePath;
		log.debug("filePath [{}]", filePath);
		try (InputStream in = new ByteArrayInputStream(documentByteArr)) {
			dropBoxClient.files().uploadBuilder(filePath).withMode(WriteMode.OVERWRITE)
					.uploadAndFinish(in);
			return true;
		}
	}

	@Override
	public byte[] downloadFile(String filePath) throws DbxException, IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			dropBoxClient.files().download(basePath + filePath).download(outputStream);
			return outputStream.toByteArray();
		}
	}

	@Override
	public void removeFile(String filePath) throws Exception {
		dropBoxClient.files().deleteV2(filePath);
	}

	@Override
	public WMSFileUploadPlatform getPlatformType() {
		// TODO Auto-generated method stub
		return WMSFileUploadPlatform.DROPBOX;
	}

	@Override
	public void mkdir(String folderPath) throws Exception {
		dropBoxClient.files().createFolderV2(folderPath);
	}

	@Override
	public void renameFolder(String oldPath, String newPath) throws Exception {
		dropBoxClient.files().moveV2(oldPath, newPath);

	}

	@Override
	public void move(String sourcePath, String destinationPath) throws Exception {
		dropBoxClient.files().moveV2(sourcePath, destinationPath);

	}

	@Override
	public List<String> list(String folderPath) throws Exception {
		// TODO Auto-generated method stub
		ListFolderResult result = dropBoxClient.files().listFolder(folderPath);
	    return result.getEntries().stream().map(Metadata::getName).collect(Collectors.toList());
	}

	@Override
	public void deleteFolder(String folderPath) throws Exception {
		dropBoxClient.files().deleteV2(folderPath);

	}

	@Override
	public void copy(String sourceFilePath, String targetFilePath) throws Exception {
		dropBoxClient.files().copyV2(sourceFilePath, targetFilePath);

	}
}
