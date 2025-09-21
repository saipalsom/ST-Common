package org.saipal.common.service.impl;

import java.util.List;

import javax.crypto.SecretKey;

import org.bson.types.Binary;
import org.saipal.common.config.StorageConfig;
import org.saipal.common.entity.DataMap;
import org.saipal.common.entity.DataMapKey;
import org.saipal.common.entity.STAttachment;
import org.saipal.common.entity.STFile;
import org.saipal.common.exception.STNotFoundException;
import org.saipal.common.exception.STRuntimeException;
import org.saipal.common.repo.STAttachmentRepo;
import org.saipal.common.repo.STFileRepo;
import org.saipal.common.service.STConfigurationService;
import org.saipal.common.service.STFileManager;
import org.saipal.common.service.STFileService;
import org.saipal.common.utility.AESUtils;
import org.saipal.common.utility.STDateTimeUtil;
import org.saipal.common.utility.STFileUtils;
import org.saipal.common.utility.STStringUtils;
import org.saipal.common.utility.STUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnClass(MongoRepository.class)
public class STFileManagerImpl implements STFileManager {

	@Autowired
	private StorageConfig storageConfig;

	private Boolean fileSplitEnabled;
	private Integer primaryFilePercentage;
	private Integer encryptionMode;
	@Autowired
	private STAttachmentRepo attachmentRepo;
	@Autowired
	protected STConfigurationService configurationService;
	@Autowired
	private STFileRepo fileRepo;

	@EventListener(ContextRefreshedEvent.class)
	public void init() {
		fileSplitEnabled = configurationService.getPropertyAsBoolean("file.split.enabled", false);
		primaryFilePercentage = configurationService.getPropertyAsInt("file.primary.percent", 1);
		encryptionMode = configurationService.getPropertyAsInt("file.encrypt.mode", 0);
	}

	@Deprecated
	public byte[] getImage(byte[] part1, String storeFileName, String key) throws Exception {
		if (STStringUtils.isEmpty(storeFileName)) {
			log.info("file was not splitted");
			return part1;
		}
		STFileService wMSFileService = storageConfig.getFileService();
		byte[] part2 = wMSFileService.downloadFile(storeFileName);
		if (!STStringUtils.isEmpty(key)) {
			part1 = AESUtils.decrypt(part1, AESUtils.stringToKey(key));
			part2 = AESUtils.decrypt(part2, AESUtils.stringToKey(key));
		}
		byte[] mergedArray = new byte[part1.length + part2.length];
		System.arraycopy(part1, 0, mergedArray, 0, part1.length);
		System.arraycopy(part2, 0, mergedArray, part1.length, part2.length);
		return mergedArray;
	}

	@Deprecated
	public DataMap processImage(byte[] img, String fileName, Long idL, SecretKey key) throws Exception {
		STFileService wMSFileService = storageConfig.getFileService();
		log.debug("processfile for [{}],imageSplitEnabled [{}],primaryImagePercentage [{}]", fileName, fileSplitEnabled,
				primaryFilePercentage);
		DataMap map = new DataMap(false);
		if (!fileSplitEnabled) {
			log.debug("file split is disabled");
			map.put("img", img);
			return map;
		}

		String id = String.valueOf(idL);
		log.debug("file split is enabled, primary file percentage [{}]", primaryFilePercentage);
		List<byte[]> imgList = STFileUtils.splitFile(img, primaryFilePercentage);
		if (!STStringUtils.isEmpty(fileName)) {
			id += "_" + fileName;
		}
		String storeFileName = STFileUtils.createFileName(id, img);
		map.put("store_file_name", storeFileName);
		byte[] part1 = imgList.get(0);
		byte[] part2 = imgList.get(1);
		if (key != null) {
			log.debug("file encryption is enabled");
			// Encrypt the byte arrays
			part1 = AESUtils.encrypt(part1, key);
			part2 = AESUtils.encrypt(part2, key);
		}
		if (wMSFileService.uploadFile(part2, storeFileName)) {
			map.put("img", part1);
			return map;
		} else {
			throw new STRuntimeException("cannot upload file for " + fileName);
		}

	}



	@Override
	public void upload(Long id, String img, String fileName, String filePath, Long createdBy) throws Exception {
		upload(id, img, fileName, filePath, fileSplitEnabled, encryptionMode, createdBy);
	}

	@Override
	public void upload(Long fileId, String file, String fileName, String filePath, boolean fileSplitEnabled,
			int encryptionMode, Long createdBy) throws Exception {
		upload(fileId, STFileUtils.decodeBase64(file), fileName, filePath, fileSplitEnabled, encryptionMode,
				createdBy);
	}

	@Override
	public void upload(Long id, byte[] img, String fileName, String filePath, Long createdBy) throws Exception {
		upload(id, img, fileName, filePath, fileSplitEnabled, encryptionMode, createdBy);
	}

	@Override
	public void upload(Long id, byte[] img, String fileName, String filePath, boolean fileSplitEnabled,
			int encryptionMode, Long createdBy) throws Exception {
		log.debug(
				"upload started for id[{}] img[{}], fileName[{}],filePath[{}],fileSplitEnabled[{}],encryptionMode[{}]",
				id, img == null ? null : img.length, fileName, filePath, fileSplitEnabled, encryptionMode);
		if (img == null) {
			throw new STRuntimeException("file is null");
		}
		STAttachment attachment = new STAttachment();
		STFile file = new STFile();
		SecretKey key = null;
		if (encryptionMode == 1) {
			key = AESUtils.generateAESKey();
			file.setPassword(AESUtils.keyToString(key));
		}
		byte[] part1 = null, part2 = null;
		String uploadFilePath = String.valueOf(id);
		if (!STStringUtils.isEmpty(fileName)) {
			uploadFilePath = uploadFilePath + "_" + fileName;
		}
		if (!STStringUtils.isEmpty(filePath)) {
			uploadFilePath = filePath + uploadFilePath;
		}
		if (!fileSplitEnabled) {
			log.debug("file split is disabled");
			part1 = img;

		} else {
			log.debug("file split is enabled, primary file percentage [{}]", primaryFilePercentage);
			List<byte[]> imgList = STFileUtils.splitFile(img, primaryFilePercentage);
			part1 = imgList.get(0);
			part2 = imgList.get(1);

		}
		if (key != null) {
			log.debug("file encryption is enabled");
			// Encrypt the byte arrays
			part1 = AESUtils.encrypt(part1, key);
			part2 = AESUtils.encrypt(part2, key);
		}
		if (part2 != null && part2.length > 0) {
			getFileService().uploadFile(part2, uploadFilePath);
			attachment.setPlatform(getFileService().getPlatformType());
			attachment.setIsUploaded(true);
			log.debug("upload file done in [{}]", getFileService().getPlatformType());
		}

		attachment.setId(id);
		attachment.setFilePath(uploadFilePath);
		attachment.setData(new Binary(part1));
		attachment.setIsAttachmentSplit(this.fileSplitEnabled);
		attachmentRepo.save(attachment);

		file.setId(id);
		file.setFileName(fileName);
		file.setFileSize(STFileUtils.getFileSize(img));
		file.setMimeType(STFileUtils.detectFileType(img));
		file.setCreatedAt(STDateTimeUtil.getCurrentDateUtc());
		file.setCreatedBy(createdBy);
		fileRepo.save(file);

	}

	@Override
	public DataMap download(Long id) throws Exception {
		DataMap result = new DataMap(false);
		STFile file = fileRepo.findById(id).orElseThrow(() -> new STRuntimeException("file not found for id " + id));
		String key = file.getPassword();
		byte[] part1 = null, part2 = null, mergedArray;
		STAttachment attachment = attachmentRepo.findById(id)
				.orElseThrow(() -> new STRuntimeException("attachment not found for id " + id));
		part1 = attachment.getData().getData();
		// 1. check if the file was splitted
		if (attachment.getIsUploaded()) {
			log.debug("file split was enabled and uploaded, get splitted file");
			part2 = getFileService().downloadFile(attachment.getFilePath());
		}
		// 2. check if the file was encrypted
		if (!STStringUtils.isEmpty(key)) {
			log.debug("encyption was enabled, performing decryption");
			part1 = AESUtils.decrypt(part1, AESUtils.stringToKey(key));
			part2 = AESUtils.decrypt(part2, AESUtils.stringToKey(key));
		}
		if (part2 == null || part2.length == 0) {
			mergedArray = part1;
		} else {
			mergedArray = new byte[part1.length + part2.length];
			System.arraycopy(part1, 0, mergedArray, 0, part1.length);
			System.arraycopy(part2, 0, mergedArray, part1.length, part2.length);
		}
		result.put(DataMapKey.FILE_NAME, file.getFileName());
		result.put(DataMapKey.FILE_CONTENT, mergedArray);
		result.put(DataMapKey.FILE_CONTENT_BASE64, STFileUtils.encodeBase64(mergedArray));
		result.put(DataMapKey.FILE_CREATED_AT, STDateTimeUtil.formatDateToString(file.getCreatedAt()));
		return result;
	}

	@Override
	public void delete(Long fileId, Long deletedBy) throws Exception {
		STFileService sTFileService = getFileService();
		STFile file = fileRepo.findById(fileId)
				.orElseThrow(() -> new STNotFoundException("file not found for id: " + fileId));
		STAttachment attachment = attachmentRepo.findById(fileId)
				.orElseThrow(() -> new STNotFoundException("attachment not found for id: " + fileId));
		sTFileService.removeFile(attachment.getFilePath());
		file.setDeleteBy(deletedBy);
		file.setDeleteAt(STDateTimeUtil.getCurrentDateUtc());
		fileRepo.save(file);

	}

	@Override
	public void copy(Long sourceFileId,Long targetFileId, String targetFilePath, Long copiedBy) throws Exception {
		STFile file = fileRepo.findById(sourceFileId)
				.orElseThrow(() -> new STNotFoundException("file not found for id: " + sourceFileId));
		STAttachment attachment = attachmentRepo.findById(sourceFileId)
				.orElseThrow(() -> new STNotFoundException("attachment not found for id: " + sourceFileId));
		getFileService().copy(attachment.getFilePath(), targetFilePath);
		STFile newFile = STUtils.deepCopy(file, STFile.class);
		newFile.setId(targetFileId);
		newFile.setCreatedBy(copiedBy);
		newFile.setCreatedAt(STDateTimeUtil.getCurrentDateUtc());
		fileRepo.save(newFile);
		STAttachment newAttachment = STUtils.deepCopy(attachment, STAttachment.class);
		newAttachment.setFilePath(targetFilePath);
		newAttachment.setId(targetFileId);
		attachmentRepo.save(newAttachment);
	}
	@Override
	public void move(Long fileId, String destinationPath) throws Exception {
		STAttachment attachment = attachmentRepo.findById(fileId)
				.orElseThrow(() -> new STNotFoundException("attachment not found for id: " + fileId));
		getFileService().move(attachment.getFilePath(), destinationPath);
		attachment.setFilePath(destinationPath);
		attachmentRepo.save(attachment);

	}

	@Override
	public void mkdir(String folderPath) throws Exception {
		getFileService().mkdir(folderPath);

	}

	@Override
	public void renameFolder(String oldPath, String newPath) throws Exception {
		getFileService().renameFolder(oldPath, newPath);

	}

	@Override
	public void deleteFolder(String folderPath) throws Exception {
		getFileService().deleteFolder(folderPath);
	}


	@Override
	public List<String> list(String folderPath) throws Exception {
		return getFileService().list(folderPath);
	}

	private STFileService getFileService() {
		return storageConfig.getFileService();
	}

}
