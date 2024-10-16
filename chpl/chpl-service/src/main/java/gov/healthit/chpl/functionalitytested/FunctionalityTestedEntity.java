package gov.healthit.chpl.functionalitytested;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.criteriaattribute.rule.RuleEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.PracticeTypeEntity;
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
@Table(name = "functionality_tested")
public class FunctionalityTestedEntity extends EntityAudit {
    private static final long serialVersionUID = 2662883108826795645L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number")
    private String number;

    @Basic(optional = false)
    @Column(name = "value")
    private String value;

    @Basic(optional = true)
    @Column(name = "regulatory_text_citation")
    private String regulatoryTextCitation;

    @Basic(optional = true)
    @Column(name = "additional_information")
    private String additionalInformation;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Basic(optional = true)
    @Column(name = "required_day")
    private LocalDate requiredDay;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_type_id", insertable = true, updatable = true)
    private PracticeTypeEntity practiceType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "functionalityTestedId")
    @Basic(optional = false)
    @Column(name = "functionality_tested_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<FunctionalityTestedCriteriaMapEntity> mappedCriteria = new HashSet<FunctionalityTestedCriteriaMapEntity>();

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;


    public FunctionalityTested toDomain() {
        return FunctionalityTested.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .practiceType(this.getPracticeType() != null ? this.getPracticeType().toDomain() : null)
                .build();
    }

    public FunctionalityTested toDomainWithCriteria(CertificationCriterionComparator criterionComparator) {
        return FunctionalityTested.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .practiceType(this.getPracticeType() != null ? this.getPracticeType().toDomain() : null)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .sorted(criterionComparator)
                        .collect(Collectors.toCollection(ArrayList::new)) : null)
                .build();
    }
}
