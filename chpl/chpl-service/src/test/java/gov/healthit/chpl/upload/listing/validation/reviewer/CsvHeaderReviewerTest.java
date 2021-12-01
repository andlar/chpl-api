package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadTestUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class CsvHeaderReviewerTest {
    private static final String HEADER_V20 = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C,PRODUCT__C,VERSION__C,CERT_YEAR__C,ACB_CERTIFICATION_ID__C,CERTIFYING_ACB__C,TESTING_ATL__C,CERTIFICATION_DATE__C,VENDOR_STREET_ADDRESS__C,VENDOR_STATE__C,VENDOR_CITY__C,VENDOR_ZIP__C,VENDOR_WEBSITE__C,Self-developer,VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C,Developer-Identified Target Users,QMS Standard,QMS Standard Applicable Criteria,QMS Modification Description,ICS,ICS Source,Accessibility Certified,Accessibility Standard,170.523(k)(1) URL,CQM Number,CQM Version,CQM Criteria,SED Report Hyperlink,Description of the Intended Users,Date SED Testing was Concluded,Participant Identifier,Participant Gender,Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs,Task Identifier,Task Description,Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,Task Rating - Standard Deviation,CRITERIA_170_315_A_1__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_2__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_3__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_4__C,GAP,Privacy and Security Framework,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_10__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_12__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_13__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_14__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_15__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_B_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_1_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_2__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_2_Cures__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_6__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_7_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_8__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_8_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_10__C,Privacy and Security Framework,Export Documentation,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_C_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_D_1__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_4__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_5__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_6__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_7__C,GAP,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_8__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_9__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_11__C,GAP,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_12__C,Attestation Answer,Documentation URL,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_D_13__C,Attestation Answer,Documentation URL,Use Cases,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_E_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_1_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_2__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_E_3__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_3__C,GAP,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_5_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_6__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_F_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_1__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_2__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_3__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_4__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_5__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_6__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_6_CURES__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_7__C,Privacy and Security Framework,API Documentation Link,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_G_8__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_9__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_10__C,Privacy and Security Framework,Standard Tested Against,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description";
    private static final String HEADER_V19 = "UNIQUE_CHPL_ID__C,RECORD_STATUS__C,VENDOR__C,PRODUCT__C,VERSION__C,CERT_YEAR__C,ACB_CERTIFICATION_ID__C,CERTIFYING_ACB__C,TESTING_ATL__C,CERTIFICATION_DATE__C,VENDOR_STREET_ADDRESS__C,VENDOR_STATE__C,VENDOR_CITY__C,VENDOR_ZIP__C,VENDOR_WEBSITE__C,Self-developer,VENDOR_EMAIL__C,VENDOR_PHONE__C,VENDOR_CONTACT_NAME__C,Developer-Identified Target Users,QMS Standard,QMS Standard Applicable Criteria,QMS Modification Description,ICS,ICS Source,Accessibility Certified,Accessibility Standard,170.523(k)(1) URL,CQM Number,CQM Version,CQM Criteria,SED Report Hyperlink,Description of the Intended Users,Date SED Testing was Concluded,Participant Identifier,Participant Gender,Participant Age,Participant Education,Participant Occupation/Role,Participant Professional Experience,Participant Computer Experience,Participant Product Experience,Participant Assistive Technology Needs,Task Identifier,Task Description,Task Success - Mean (%),Task Success - Standard Deviation (%),Task Path Deviation - Observed #,Task Path Deviation - Optimal #,Task Time - Mean (seconds),Task Time - Standard Deviation (seconds),Task Time Deviation - Observed Seconds,Task Time Deviation - Optimal Seconds,Task Errors  Mean(%),Task Errors - Standard Deviation (%),Task Rating - Scale Type,Task Rating,Task Rating - Standard Deviation,CRITERIA_170_315_A_1__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_2__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_3__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_4__C,GAP,Privacy and Security Framework,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_10__C,GAP,Privacy and Security Framework,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_12__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_13__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_A_14__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_A_15__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_B_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_1_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_2__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_2_Cures__C,Privacy and Security Framework,Standard Tested Against,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_3_Cures__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,UCD Process Selected,UCD Process Details,Task Identifier,Participant Identifier,CRITERIA_170_315_B_6__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_7_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_B_8__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_8_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_B_10__C,Privacy and Security Framework,Export Documentation,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_C_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_3_CURES__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_C_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_D_1__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_2_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_3_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_4__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_5__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_6__C,GAP,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_7__C,GAP,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_8__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_9__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_10_Cures__C,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_11__C,GAP,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_D_12__C,Attestation Answer,Documentation URL,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_D_13__C,Attestation Answer,Documentation URL,Use Cases,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_E_1__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_1_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_E_2__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_E_3__C,Privacy and Security Framework,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_3__C,GAP,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_4__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_F_5__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_5_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_F_6__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,CRITERIA_170_315_F_7__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_1__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_2__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_3__C,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_4__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_5__C,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_6__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_6_CURES__C,Standard Tested Against,Functionality Tested,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_7__C,Privacy and Security Framework,API Documentation Link,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test procedure version,CRITERIA_170_315_G_8__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test Procedure,Test procedure version,CRITERIA_170_315_G_9__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_9_CURES__C,Privacy and Security Framework,Standard Tested Against,Functionality Tested,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_G_10__C,Privacy and Security Framework,Standard Tested Against,API Documentation Link,Measure Successfully Tested for G1,Measure Successfully Tested for G2,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_1__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description,CRITERIA_170_315_H_2__C,Privacy and Security Framework,Standard Tested Against,Additional Software,CP Source,CP Source Grouping,Non CP Source,Non CP Source Version,Non CP Source Grouping,Test tool name,Test tool version,Test Procedure,Test procedure version,Test Data,Test data version,Test data alteration,Test data alteration description";

    private static final String CSV_INVALID_HEADER = "The heading '%s' was found in the upload file but is not recognized.";
    private static final String CSV_DUPLICATE_HEADER = "The heading '%s' appears to be a duplicate in the file.";
    private static final String CSV_DUPLICATE_CRITERIA_HEADER = "The heading '%s' appears to be a duplicate for certification result %s.";

    private ErrorMessageUtil errorMessageUtil;
    private CSVHeaderReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.unrecognizedHeading"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_INVALID_HEADER, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.duplicateHeading"), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_DUPLICATE_HEADER, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.upload.duplicateCriteriaHeading"), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(i -> String.format(CSV_DUPLICATE_CRITERIA_HEADER, i.getArgument(1), i.getArgument(2)));

        ListingUploadHandlerUtil uploadHandlerUtil = new ListingUploadHandlerUtil(errorMessageUtil);
        reviewer = new CSVHeaderReviewer(uploadHandlerUtil, errorMessageUtil);
    }

    @Test
    public void review_nullHeader_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(null)
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_emptyHeader_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(new ArrayList<CSVRecord>())
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_blankHeaderValue_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_v19Header_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString(HEADER_V19))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_v20Header_noWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString(HEADER_V20))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(0, listing.getWarningMessages().size());
    }

    @Test
    public void review_invalidHeadingInMiddle_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNKNOWNVALUE,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_invalidHeadingInBeginning_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNKNOWNVALUE,UNIQUE_CHPL_ID__C,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_invalidHeadingAtEnd_hasWarning() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,UNKNOWNVALUE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
    }

    @Test
    public void review_multiplIinvalidHeadings_hasWarnings() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNKNOWNVALUE__C,VENDOR__C,UNKNOWNVALUE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getWarningMessages().size());
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE", "")));
        assertTrue(listing.getWarningMessages().contains(String.format(CSV_INVALID_HEADER, "UNKNOWNVALUE__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacent_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacentAtBeginning_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "UNIQUE_CHPL_ID__C", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsAdjacentAtEnd_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_PHONE", "")));
    }

    @Test
    public void review_oneDuplicateListingLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,PRODUCT,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT", "")));
    }

    @Test
    public void review_twoDuplicateListingLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,DEVELOPER,VENDOR__C,PRODUCT__C,PRODUCT,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "PRODUCT", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
    }

    @Test
    public void review_multipleDuplicateListingLevelHeadings_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR_WEBSITE,VENDOR__C,PRODUCT__C,UNIQUE_CHPL_ID__C,VENDOR_WEBSITE,VENDOR_EMAIL,VENDOR_PHONE,VENDOR_PHONE"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_PHONE", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR_WEBSITE", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "UNIQUE_CHPL_ID__C", "")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_SingleCriteriaAndHeadingsAdjacent_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,GAP,Test Data,Test Procedure"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_SingleCriteriaAndHeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_MultipleCriteriaAndHeadingsApart_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_oneDuplicateCertResultLevelHeading_HeadingsHaveDifferentStringsMappedToSameValue_hasError() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,Standard Tested Against,Optional Standard"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Optional Standard", "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_twoDuplicateCertResultLevelHeadings_MultipleCriteria_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }

    @Test
    public void review_multipleDuplicateCertResultLevelHeadings_MultipleCriteriaWithSameDuplicatedColumn_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,GAP,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(3, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_9__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }

    @Test
    public void review_oneDuplicateCriterion_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,Test Data,Test Procedure,CRITERIA_170_315_A_8__C,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_8__C")));
    }

    @Test
    public void review_twoDuplicateCriteria_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,CRITERIA_170_315_A_8__C,Test Data,Test Procedure,CRITERIA_170_315_A_8__C,GAP,Additional Software,Privacy and Security Framework,CRITERIA_170_315_A_1__C,CRITERIA_170_315_A_1__C"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(2, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "CRITERIA_170_315_A_1__C")));
    }

    @Test
    public void review_multipleDuplicateListingAndCertResultLevelHeadings_hasErrors() {
        ListingUpload listingUploadMetadata = ListingUpload.builder()
                .records(ListingUploadTestUtil.getRecordsFromString("UNIQUE_CHPL_ID__C,VENDOR__C,PRODUCT__C,VENDOR__C,CRITERIA_170_315_A_8__C,GAP,Test Data,Test Procedure,GAP,CRITERIA_170_315_A_9__C,GAP,Privacy and Security Framework,GAP,Additional Software,Privacy and Security Framework"))
                .build();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder().build();
        reviewer.review(listingUploadMetadata, listing);

        assertEquals(4, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_HEADER, "VENDOR__C", "")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_8__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "GAP", "CRITERIA_170_315_A_9__C")));
        assertTrue(listing.getErrorMessages().contains(String.format(CSV_DUPLICATE_CRITERIA_HEADER, "Privacy and Security Framework", "CRITERIA_170_315_A_9__C")));
    }
}
