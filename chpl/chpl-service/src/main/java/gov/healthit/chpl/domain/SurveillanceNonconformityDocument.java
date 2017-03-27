package gov.healthit.chpl.domain;

import java.io.Serializable;

public class SurveillanceNonconformityDocument implements Serializable {
	private static final long serialVersionUID = -7456509117016763596L;
	private Long id;
	private String fileName;
	private String fileType;
	private byte[] fileContents;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public byte[] getFileContents() {
		return fileContents;
	}
	public void setFileContents(byte[] fileContents) {
		this.fileContents = fileContents;
	}
}