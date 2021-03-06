package gov.healthit.chpl.domain;

import java.io.Serializable;

public class CQMCriterion implements Serializable {
    private static final long serialVersionUID = -1847517952030827806L;
    private Long criterionId;
    private String cmsId;
    private Long cqmCriterionTypeId;
    private String cqmDomain;
    private Long cqmVersionId;
    private String cqmVersion;
    private String description;
    private String nqfNumber;
    private String number;
    private String title;

    public Long getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(final Long criterionId) {
        this.criterionId = criterionId;
    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(final String cmsId) {
        this.cmsId = cmsId;
    }

    public Long getCqmCriterionTypeId() {
        return cqmCriterionTypeId;
    }

    public void setCqmCriterionTypeId(final Long cqmCriterionTypeId) {
        this.cqmCriterionTypeId = cqmCriterionTypeId;
    }

    public String getCqmDomain() {
        return cqmDomain;
    }

    public void setCqmDomain(final String cqmDomain) {
        this.cqmDomain = cqmDomain;
    }

    public Long getCqmVersionId() {
        return cqmVersionId;
    }

    public void setCqmVersionId(final Long cqmVersionId) {
        this.cqmVersionId = cqmVersionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(final String nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getCqmVersion() {
        return cqmVersion;
    }

    public void setCqmVersion(final String cqmVersion) {
        this.cqmVersion = cqmVersion;
    }

}
