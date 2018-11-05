package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingRequiredDataReviewer")
public class RequiredDataReviewer implements Reviewer {
    private static final String G3_2014 = "170.314 (g)(3)";
    private static final String G3_2015 = "170.315 (g)(3)";
    
    @Autowired protected ErrorMessageUtil msgUtil;
    @Autowired protected CertificationResultRules certRules;
    
    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationEditionId() == null && StringUtils.isEmpty(listing.getCertificationEdition())) {
            listing.getErrorMessages().add("Certification edition is required but was not found.");
        }
        if (listing.getCertificationDate() == null) {
            listing.getErrorMessages().add("Certification date was not found.");
        }
        if (listing.getCertificationBodyId() == null) {
            listing.getErrorMessages().add("ONC-ACB is required but was not found.");
        }
        if (StringUtils.isEmpty(listing.getUniqueId())) {
            listing.getErrorMessages().add("The product unique id is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperName())) {
            listing.getErrorMessages().add("A developer name is required.");
        }
        if (StringUtils.isEmpty(listing.getProductName())) {
            listing.getErrorMessages().add("A product name is required.");
        }
        if (StringUtils.isEmpty(listing.getProductVersion())) {
            listing.getErrorMessages().add("A product version is required.");
        }
        if (listing.getDeveloperAddress() != null) {
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getStreetLineOne())) {
                listing.getErrorMessages().add("Developer street address is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getCity())) {
                listing.getErrorMessages().add("Developer city is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getState())) {
                listing.getErrorMessages().add("Developer state is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperAddress().getZipcode())) {
                listing.getErrorMessages().add("Developer zip code is required.");
            }
        } else {
            if (StringUtils.isEmpty(listing.getDeveloperStreetAddress())) {
                listing.getErrorMessages().add("Developer street address is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperCity())) {
                listing.getErrorMessages().add("Developer city is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperState())) {
                listing.getErrorMessages().add("Developer state is required.");
            }
            if (StringUtils.isEmpty(listing.getDeveloperZipCode())) {
                listing.getErrorMessages().add("Developer zip code is required.");
            }
        }
        if (StringUtils.isEmpty(listing.getDeveloperWebsite())) {
            listing.getErrorMessages().add("Developer website is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperEmail())) {
            listing.getErrorMessages().add("Developer contact email address is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperPhoneNumber())) {
            listing.getErrorMessages().add("Developer contact phone number is required.");
        }
        if (StringUtils.isEmpty(listing.getDeveloperContactName())) {
            listing.getErrorMessages().add("Developer contact name is required.");
        }
        boolean foundSedCriteria = false;
        boolean attestsToSed = false;

        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getMeetsCriteria() == null) {
                listing.getErrorMessages()
                .add("0 or 1 is required to inidicate whether " + cert.getNumber() + " was met.");
            } else if (cert.getMeetsCriteria().booleanValue()) {
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP) && cert.getGap() == null) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingGap", cert.getNumber()));
                }

                boolean gapEligibleAndTrue = false;
                if (certRules.hasCertOption(cert.getNumber(), CertificationResultRules.GAP)
                        && cert.getGap() != null && cert.getGap().booleanValue()) {
                    gapEligibleAndTrue = true;
                }

                if (!gapEligibleAndTrue
                        && certRules.hasCertOption(cert.getNumber(), CertificationResultRules.TEST_PROCEDURE)
                        && (cert.getTestProcedures() == null || cert.getTestProcedures().size() == 0)) {
                    listing.getErrorMessages().add(
                            msgUtil.getMessage("listing.criteria.missingTestProcedure",
                            cert.getNumber()));
                }
                if (cert.getSed() != null && cert.getSed().booleanValue()) {
                    foundSedCriteria = true;
                }
                if (cert.getNumber().equalsIgnoreCase(G3_2014)
                        || cert.getNumber().equalsIgnoreCase(G3_2015)) {
                    attestsToSed = true;
                }
            }
        }
        if (foundSedCriteria && !attestsToSed) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        }
        if (!foundSedCriteria && attestsToSed) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.foundNoSedCriteriaButAttestingSed"));
        }
    }
}