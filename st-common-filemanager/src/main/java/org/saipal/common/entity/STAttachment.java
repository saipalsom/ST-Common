package org.saipal.common.entity;

import org.bson.types.Binary;
import org.saipal.common.entity.STFileUploadConfig.WMSFileUploadPlatform;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("st_attachment")
public class STAttachment {

	private Long id;
	private Binary data;
	private Boolean isAttachmentSplit;
	private Boolean isUploaded;
	private String filePath;
	@Enumerated(EnumType.STRING)
	private WMSFileUploadPlatform platform;

}
