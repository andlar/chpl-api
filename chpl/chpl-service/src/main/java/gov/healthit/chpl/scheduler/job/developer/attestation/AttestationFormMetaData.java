package gov.healthit.chpl.scheduler.job.developer.attestation;

public class AttestationFormMetaData {
    private static final Long INFO_BLOCKING_CONDITION = 1L;
    private static final Long ASSURANCES_CONDITION = 7L;
    private static final Long ASSURANCES_CONDITION_ORIG = 2L;
    private static final Long COMMUNICATIONS_CONDITION = 3L;
    private static final Long API_CONDITION = 4L;
    private static final Long RWT_CONDITION = 5L;

    private static final Long NOT_APPLICABLE_RESPONSE_ID = 3L;
    private static final Long NON_COMPLIANT_RESPONSE_ID = 2L;
    private static final Long COMPLIANT_RESPONSE_ID = 1L;
    private static final Long ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSE_ID_ORIG = 4L;
    private static final Long ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSE_ID = 9L;
    private static final Long ASSURANCES_COMPLIANT_IS_NOT_APPLICABLE_RESPONSE_ID_ORIG = 5L;
    private static final Long ASSURANCES_COMPLIANT_IS_NOT_APPLICABLE_RESPONSE_ID = 10L;

    public static Long getInformationBlockingConditionId() {
        return INFO_BLOCKING_CONDITION;
    }

    public static Long getAssurancesConditionId(Long attestationPeriodId) {
        // OCD-4134 will address this in a better manner
        if (attestationPeriodId <= 3L) {
            return ASSURANCES_CONDITION_ORIG;
        } else {
            return ASSURANCES_CONDITION;
        }
    }

    public static Long getCommunicationConditionId() {
        return COMMUNICATIONS_CONDITION;
    }

    public static Long getApiConditionId() {
        return API_CONDITION;
    }

    public static Long getRwtConditionId() {
        return RWT_CONDITION;
    }

    public static Long getNotApplicableResponseId() {
        return NOT_APPLICABLE_RESPONSE_ID;
    }

    public static Long getCompliantResponseId() {
        return COMPLIANT_RESPONSE_ID;
    }

    public static Long getNonCompliantResponseId() {
        return NON_COMPLIANT_RESPONSE_ID;
    }

    public static Long getAssurancesCompliantIsApplicableResponseId(Long attestationPeriodId) {
        // OCD-4134 will address this in a better manner
        if (attestationPeriodId <= 3L) {
            return ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSE_ID_ORIG;
        } else {
            return ASSURANCES_COMPLIANT_IS_APPLICABLE_RESPONSE_ID;
        }
    }

    public static Long getAssurancesCompliantIsNotApplicableResponseId(Long attestationPeriodId) {
        // OCD-4134 will address this in a better manner
        if (attestationPeriodId <= 3L) {
            return ASSURANCES_COMPLIANT_IS_NOT_APPLICABLE_RESPONSE_ID_ORIG;
        } else {
            return ASSURANCES_COMPLIANT_IS_NOT_APPLICABLE_RESPONSE_ID;
        }
    }
}
