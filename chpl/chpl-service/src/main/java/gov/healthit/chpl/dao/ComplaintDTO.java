package gov.healthit.chpl.dao;

import java.util.Date;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.entity.ComplaintEntity;

public class ComplaintDTO {
    private Long id;
    private CertificationBodyDTO certificationBody;
    private ComplaintTypeDTO complaintType;
    private ComplaintStatusTypeDTO complaintStatusType;
    private String oncComplaintId;
    private Date receivedDate;
    private String summary;
    private String actions;
    private boolean complainantContacted;
    private boolean developerContacted;
    private boolean oncAtlContacted;
    private Date closedDate;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ComplaintDTO(ComplaintEntity entity) {
        this.id = entity.getId();
        this.setCertificationBody(new CertificationBodyDTO(entity.getCertificationBody()));
        this.complaintType = new ComplaintTypeDTO(entity.getComplaintType());
        this.complaintStatusType = new ComplaintStatusTypeDTO(entity.getComplaintStatusType());
        this.oncComplaintId = entity.getOncComplaintId();
        this.receivedDate = entity.getReceivedDate();
        this.summary = entity.getSummary();
        this.actions = entity.getActions();
        this.complainantContacted = entity.isComplainantContacted();
        this.developerContacted = entity.isDeveloperContacted();
        this.oncAtlContacted = entity.isOncAtlContacted();
        this.closedDate = entity.getClosedDate();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBodyDTO getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(CertificationBodyDTO certificationBody) {
        this.certificationBody = certificationBody;
    }

    public ComplaintTypeDTO getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(final ComplaintTypeDTO complaintType) {
        this.complaintType = complaintType;
    }

    public ComplaintStatusTypeDTO getComplaintStatusType() {
        return complaintStatusType;
    }

    public void setComplaintStatusType(final ComplaintStatusTypeDTO complaintStatusType) {
        this.complaintStatusType = complaintStatusType;
    }

    public String getOncComplaintId() {
        return oncComplaintId;
    }

    public void setOncComplaintId(final String oncComplaintId) {
        this.oncComplaintId = oncComplaintId;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(final String actions) {
        this.actions = actions;
    }

    public boolean isComplainantContacted() {
        return complainantContacted;
    }

    public void setComplainantContacted(final boolean complainantContacted) {
        this.complainantContacted = complainantContacted;
    }

    public boolean isDeveloperContacted() {
        return developerContacted;
    }

    public void setDeveloperContacted(final boolean developerContacted) {
        this.developerContacted = developerContacted;
    }

    public boolean isOncAtlContacted() {
        return oncAtlContacted;
    }

    public void setOncAtlContacted(final boolean oncAtlContacted) {
        this.oncAtlContacted = oncAtlContacted;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(final Date closedDate) {
        this.closedDate = closedDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

}
