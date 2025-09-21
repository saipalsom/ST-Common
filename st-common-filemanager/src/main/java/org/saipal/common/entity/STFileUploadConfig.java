package org.saipal.common.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "file_upload_config")
@Data
public class STFileUploadConfig {
	@Id
	private Long id;
	private String configKey;
	@Enumerated(EnumType.STRING)
	private WMSFileUploadPlatform platformType;
	@Column(columnDefinition = "LONGTEXT")
	private String configValue;
	private boolean isActive;
	private Date createdDateTime;
	private boolean isDefault;

	public enum WMSFileUploadPlatform {
		GOOGLE_DRIVE, DROPBOX, LOCAL, GOOGLE_CLOUD
	}
}