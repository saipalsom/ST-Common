package org.saipal.common.service;

import java.util.List;

import org.saipal.common.entity.DataMap;


public interface STFileManager {

	/**
	 * Upload base64 file, uses default config of application for image
	 * split(fileSplitEnabled=file.split.enabled) and encryption
	 * mode(encryptionMode=file.encrypt.mode)
	 * 
	 * @param fileId    id of file for database
	 * @param data      base64 data
	 * @param fileName  name of file
	 * @param filePath  path of file
	 * @param createdBy user id of user creating file
	 * @throws Exception throws exception if cannot upload file
	 */
	void upload(Long fileId, String data, String fileName, String filePath, Long createdBy) throws Exception;

	/**
	 * Upload byte array file, uses default config of application for image
	 * split(fileSplitEnabled=file.split.enabled) and encryption
	 * mode(encryptionMode=file.encrypt.mode)
	 * 
	 * @param fileId    id of file for database
	 * @param data      byte array data
	 * @param fileName  name of file
	 * @param filePath  path of file
	 * @param createdBy user id of user creating file
	 * @throws Exception throws exception if cannot upload file
	 */
	void upload(Long fileId, byte[] data, String fileName, String filePath, Long createdBy) throws Exception;

	/**
	 * Upload byte array file
	 * 
	 * @param fileId           id of file for database
	 * @param data             byte array data
	 * @param fileName         name of file
	 * @param filePath         path of file
	 * @param createdBy        user id of user creating file
	 * @param fileSplitEnabled flag to enable splitting of file, if enabled, file
	 *                         will be split into two part, primary part will be
	 *                         stored in database(file.primary.percent) and other
	 *                         part to be upload to platform configured for current
	 *                         user organization
	 * @param encryptionMode   mode of encryption: currently supported value 0-> no
	 *                         encryption, 1 -> encryption after split
	 * @throws Exception throws exception if cannot upload file
	 */
	void upload(Long fileId, byte[] data, String fileName, String filePath, boolean fileSplitEnabled,
			int encryptionMode, Long createdBy) throws Exception;

	/**
	 * Upload base64 file
	 * 
	 * @param fileId           id of file for database
	 * @param data             base64 data
	 * @param fileName         name of file
	 * @param filePath         path of file
	 * @param createdBy        user id of user creating file
	 * @param fileSplitEnabled flag to enable splitting of file, if enabled, file
	 *                         will be split into two part, primary part will be
	 *                         stored in database(file.primary.percent) and other
	 *                         part to be upload to platform configured for current
	 *                         user organization
	 * @param encryptionMode   mode of encryption: currently supported value 0-> no
	 *                         encryption, 1 -> encryption after split
	 * @throws Exception throws exception if cannot upload file
	 */
	void upload(Long fileId, String data, String fileName, String filePath, boolean fileSplitEnabled,
			int encryptionMode, Long createdBy) throws Exception;

	DataMap download(Long id) throws Exception;

	/**
	 * Deletes a file and its metadata.
	 */
	void delete(Long fileId, Long deletedBy) throws Exception;

	/**
	 * Copies a file to another location/storage.
	 */
	void copy(Long sourceFileId,Long targetFileId,  String targetFilePath, Long copiedBy) throws Exception;

	/**
	 * Creates a new folder in the specified storage.
	 */
	void mkdir(String folderPath) throws Exception;

	/**
	 * Renames a folder in the specified storage.
	 *
	 * @param oldPath   Current folder path.
	 * @param newPath   New folder name or path.
	 * @param updatedBy User ID performing the rename.
	 * @throws Exception If renaming fails.
	 */
	void renameFolder(String oldPath, String newPath) throws Exception;

	/**
	 * Deletes a folder and its contents if applicable.
	 *
	 * @param folderPath Path of the folder to delete.
	 * @param deletedBy  User ID performing the deletion.
	 * @throws Exception If deletion fails.
	 */
	void deleteFolder(String folderPath) throws Exception;

	/**
	 * Moves a file or folder from one path to another.
	 *
	 * @param sourcePath      Source file or folder path.
	 * @param destinationPath Destination file or folder path.
	 * @param movedBy         User ID performing the move.
	 * @throws Exception If move operation fails.
	 */
	void move(Long fileId, String destinationPath) throws Exception;

	/**
	 * Lists all files and folders under a specified path.
	 *
	 * @param folderPath Path to list contents of.
	 * @return List of file/folder names or metadata.
	 * @throws Exception If listing fails.
	 */
	List<String> list(String folderPath) throws Exception;

}
