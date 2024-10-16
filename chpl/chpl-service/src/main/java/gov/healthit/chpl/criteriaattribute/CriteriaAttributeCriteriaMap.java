package gov.healthit.chpl.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaAttributeCriteriaMap {
    private Long id;
    private CriteriaAttribute criteriaAttribute;
    private CertificationCriterion criterion;
}
