package gov.healthit.chpl.scheduler.job.urlStatus.data;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "url_check_result")
public class UrlResultEntity extends EntityAudit {
    private static final long serialVersionUID = 600144930563463240L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new SystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "url_type_id")
    private Long urlTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "url_type_id", insertable = false, updatable = false)
    private UrlTypeEntity urlType;

    @Basic(optional = false)
    @Column(name = "response_code")
    private Integer responseCode;

    @Basic(optional = false)
    @Column(name = "response_message")
    private String responseMessage;

    @Basic(optional = false)
    @Column(name = "checked_date")
    private Date lastChecked;

}
