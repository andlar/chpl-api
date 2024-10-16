package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class TestingLabReviewerTest {
    private static final String MISSING_ATL = "Testing Lab is required but not found.";
    private static final String MISSING_ATL_NAME = "Testing Lab name is required but was not found.";
    private static final String INVALID_ATL = "The ONC-ATL %s is not valid.";
    private static final String NOT_FOUND = "Testing lab not found.";

    private ErrorMessageUtil errorMessageUtil;
    private TestingLabReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidTestingLab"), ArgumentMatchers.anyString()))
        .thenAnswer(i -> String.format(INVALID_ATL, i.getArgument(1), ""));
        Mockito.when(errorMessageUtil.getMessage("listing.missingTestingLab"))
            .thenReturn(MISSING_ATL);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingTestingLabName")))
            .thenReturn(MISSING_ATL_NAME);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("atl.notFound")))
            .thenReturn(NOT_FOUND);
        reviewer = new TestingLabReviewer(new ChplProductNumberUtil(), errorMessageUtil);
    }

    @Test
    public void review_nullAtlLegacyListing_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_nullAtl_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.04.04.2526.WEBe.06.01.1.210102")
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL));
    }

    @Test
    public void review_atlObjectWithNullValues_hasErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder().build())
                .build();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithEmptyValues_hasErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .atlCode("")
                        .name("")
                        .build())
                .build();
        testingLabs.add(atl);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithNullNameValidId_hasError() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .atlCode("04")
                        .name("")
                        .id(1L)
                        .build())
                .build();
        testingLabs.add(atl);
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_ATL_NAME));
    }

    @Test
    public void review_atlObjectWithValidNameNullId_hasError() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .atlCode("05")
                        .name("My ATL")
                        .id(null)
                        .build())
                .build();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(INVALID_ATL, "My ATL", "")));
    }

    @Test
    public void review_atlObjectWithValidNameAndId_noErrors() {
        List<CertifiedProductTestingLab> testingLabs = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductTestingLab atl = CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .atlCode("05")
                        .name("My ATL")
                        .id(2L)
                        .build())
                .build();
        testingLabs.add(atl);

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(testingLabs)
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }
}
