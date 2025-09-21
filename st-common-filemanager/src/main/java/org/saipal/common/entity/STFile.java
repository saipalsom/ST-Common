package org.saipal.common.entity;

import java.time.Instant;

import org.saipal.common.utility.DataMapConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "st_file")
@Data

public class STFile {
	@Id
	private long id;
	private String fileName;
	private float fileSize;
	private String mimeType;
	@Column(columnDefinition = "json")
	@Convert(converter = DataMapConverter.class)
	private DataMap fileAttribute;
	private String password;
	private boolean isDelete;
	private Long createdBy;
	private Instant createdAt;
	private Long updatedBy;
	private Instant updatedAt;
	private Long deleteBy;
	private Instant deleteAt;

}
