package org.saipal.common.service;

import java.util.List;

import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;



public interface STFileService {
	/**
	 * uploads file to configured platform. Currently supported: local file
	 * system/dropbox/google drive
	 * 
	 * @param documentByteArr byte array of the document/file to be uploaded
	 * @param fileName        fileName of the document to be uploaded with path
	 * @return true if upload is successful, else false
	 * @throws Exception
	 */
	boolean uploadFile(byte[] documentByteArr, String filePath) throws Exception;

	/**
	 * retrieves document using filePath provided from configured platform
	 * 
	 * @param filePath filePath to download document
	 * @return byte array of document/file downloaded
	 * @throws Exception
	 */
	byte[] downloadFile(String filePath) throws Exception;

	/**
	 * removes file for configured platform/ currently its only supported for local
	 * file system as enabling this feature from google drive/dropbox can be harmful
	 * 
	 * @param filePath filePath to remove document
	 * @return true if file is removed else false
	 * @throws Exception
	 */

	void removeFile(String filePath) throws Exception;

	void mkdir(String folderPath) throws Exception;

	void renameFolder(String oldPath, String newPath) throws Exception;

	void move(String sourcePath, String destinationPath) throws Exception;

	List<String> list(String folderPath) throws Exception;

	void deleteFolder(String folderPath) throws Exception;

	void copy(String sourceFilePath, String targetFilePath) throws Exception;

	WMSFileUploadPlatform getPlatformType();


}
