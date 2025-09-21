package org.saipal.common.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.saipal.common.configuration.OrganizationContext;
import org.saipal.common.entity.STFileUploadConfig;
import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.saipal.common.exception.STRuntimeException;
import org.saipal.common.repo.STFileUploadConfigRepo;
import org.saipal.common.service.STConfigurationService;
import org.saipal.common.service.STFileService;
import org.saipal.common.service.impl.file.STDropBoxService;
import org.saipal.common.service.impl.file.STGoogleCloudService;
import org.saipal.common.service.impl.file.STGoogleDriveService;
import org.saipal.common.service.impl.file.STLocalFileService;
import org.saipal.common.utility.STCollectionUtils;
import org.saipal.common.utility.STStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;

@Service
public class StorageConfig {

	private Map<String, STFileService> fileServiceList;
	@Autowired
	private STFileUploadConfigRepo fileUploadConfigRepo;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	protected STConfigurationService configurationService;

	private static String defaultStorageKey;

	@EventListener(ContextRefreshedEvent.class)
	public void init() {
		List<STFileUploadConfig> fileUploadConfigs = fileUploadConfigRepo.findAll();
		if (!STCollectionUtils.isEmpty(fileUploadConfigs)) {
			fileServiceList = fileUploadConfigs.stream()
					.collect(Collectors.toMap(STFileUploadConfig::getConfigKey, conf -> setupService(conf)));
		}
		defaultStorageKey = fileUploadConfigs.stream().filter(f -> f.isDefault()).map(f -> f.getConfigKey()).findFirst()
				.orElse(null);

	}

	public STFileService getFileService() {
		String key = OrganizationContext.getOrganization();
		if (fileServiceList.containsKey(key)) {
			return fileServiceList.get(key);
		}
		Boolean allowGlobalFileService = configurationService.getPropertyAsBoolean("allow.global.file.service", false);

		if (allowGlobalFileService || OrganizationContext.isExternal()) {
			if (!STStringUtils.isEmpty(defaultStorageKey) && fileServiceList.containsKey(defaultStorageKey)) {
				// set default key global to organization, this will be later used to store
				// against the attachment, so that it can be used later to retrieve the image
				OrganizationContext.setOrganization(defaultStorageKey);
				return fileServiceList.get(defaultStorageKey);

			}
		}
		throw new STRuntimeException("file upload config not found for " + key);
	}

	public STFileService setupService(STFileUploadConfig fileUploadConfig) {
		if (WMSFileUploadPlatform.GOOGLE_DRIVE.equals(fileUploadConfig.getPlatformType())) {
			return setUpGoogleDrive(fileUploadConfig);
		} else if (WMSFileUploadPlatform.DROPBOX.equals(fileUploadConfig.getPlatformType())) {
			return setUpDropBoxService(fileUploadConfig);
		} else if (WMSFileUploadPlatform.LOCAL.equals(fileUploadConfig.getPlatformType())) {
			return setUpLocalFileUpload(fileUploadConfig);
		} else if (WMSFileUploadPlatform.GOOGLE_CLOUD.equals(fileUploadConfig.getPlatformType())) {
			return setUpGoogleCloudUpload(fileUploadConfig);
		}
		throw new STRuntimeException(fileUploadConfig.getPlatformType() + " not supported");
	}

	private STFileService setUpGoogleCloudUpload(STFileUploadConfig fileUploadConfig) {

		String[] googleCloudConfig = fileUploadConfig.getConfigValue().split(":::");
		String credentialPath = googleCloudConfig[0];
		String bucketName = googleCloudConfig[1];
		Resource credentialResource = resourceLoader.getResource(credentialPath);
		return new STGoogleCloudService(credentialResource, bucketName, fileUploadConfig.getConfigKey());
	}

	private STFileService setUpLocalFileUpload(STFileUploadConfig fileUploadConfig) {
		String localRootPath = fileUploadConfig.getConfigValue();
		return new STLocalFileService(localRootPath);
	}

	public STFileService setUpGoogleDrive(STFileUploadConfig fileUploadConfig) {
		String[] googleDriveConfig = fileUploadConfig.getConfigValue().split(":::");
		String credentialFilePath = googleDriveConfig[0];
		String driveFolderId = googleDriveConfig[1];

		Resource credentialResource = resourceLoader.getResource(credentialFilePath);
		return new STGoogleDriveService(credentialResource, driveFolderId);

	}

	public STFileService setUpDropBoxService(STFileUploadConfig fileUploadConfig) {
		String[] dropBoxConfig = fileUploadConfig.getConfigValue().split(":::");
		String ACCESS_TOKEN = dropBoxConfig[0];
		String REFRESH_TOKEN = dropBoxConfig[1];
		String APP_KEY = dropBoxConfig[2];
		String APP_SECRET = dropBoxConfig[3];
		String APP_PACKAGE = dropBoxConfig[4];
		String basePath = dropBoxConfig[5];
		DbxRequestConfig CONFIG = DbxRequestConfig.newBuilder(APP_PACKAGE).build();
		DbxCredential CREDENTIALS = new DbxCredential(ACCESS_TOKEN, 0L, REFRESH_TOKEN, APP_KEY, APP_SECRET);
		DbxClientV2 dbxClientV2 = new DbxClientV2(CONFIG, CREDENTIALS);
		return new STDropBoxService(dbxClientV2, basePath);
	}

}
