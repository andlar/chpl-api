package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.form.Form;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationPeriodForm {
    private Form form;
    private AttestationPeriod period;
}
