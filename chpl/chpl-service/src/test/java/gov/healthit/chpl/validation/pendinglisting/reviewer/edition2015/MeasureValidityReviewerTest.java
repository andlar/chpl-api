package gov.healthit.chpl.validation.pendinglisting.reviewer.edition2015;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMeasureDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.MeasureValidityReviewer;

public class MeasureValidityReviewerTest {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private MeasureValidityReviewer reviewer;

    @Before
    public void setup() {
        validationUtils = Mockito.mock(ValidationUtils.class);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        reviewer = new MeasureValidityReviewer(validationUtils, msgUtil);
    }

    @Test
    public void review_listingAttestsG1NoMeasures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG1Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(10L)
                        .number("170.315 (g)(1)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));
    }

    @Test
    public void review_listingAttestsG1HasG2Measures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG1Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(10L)
                        .number("170.315 (g)(1)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(2L)
                        .name("G2")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(1), but no measures have been successfully tested for (g)(1)."));
    }

    @Test
    public void review_listingAttestsG2NoMeasures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG2Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(11L)
                        .number("170.315 (g)(2)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().clear();

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));
    }

    @Test
    public void review_listingAttestsG2HasG1Measures_errorMessage() throws ParseException {
        Mockito.when(validationUtils.hasCert(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
            .thenCallRealMethod();
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.missingG2Measures")))
                .thenAnswer(i -> String.format(
                        "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(11L)
                        .number("170.315 (g)(2)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(
                "Listing has attested to (g)(2), but no measures have been successfully tested for (g)(2)."));
    }

    @Test
    public void review_noMeasureId_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.invalidMeasure"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Invalid G1/G2 Measure: '%s' was not found.", i.getArgument(1), ""));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .uploadedValue("BAD VALUE")
                .measure(Measure.builder()
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("Invalid G1/G2 Measure: 'BAD VALUE' was not found."));
    }

    @Test
    public void review_noMeasureTypeId_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.invalidMeasureType"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("Invalid G1/G2 Measure Type: '%s' was not found.", i.getArgument(1), ""));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .build())
                .measureType(MeasureType.builder()
                        .name("BOGUS")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("Invalid G1/G2 Measure Type: 'BOGUS' was not found."));
    }

    @Test
    public void review_noAssociatedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.missingAssociatedCriteria"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The %s measure %s for %s must have at least one associated criterion.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains("The G1 measure Test for T must have at least one associated criterion."));
    }

    @Test
    public void review_missingRequiredAssociatedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.missingRequiredCriterion"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The %s measure %s for %s is missing required criterion %s.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        Set<CertificationCriterion> allowedCriterion = new LinkedHashSet<CertificationCriterion>();
        allowedCriterion.add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        allowedCriterion.add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(allowedCriterion)
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertEquals("The G1 measure Test for T is missing required criterion 170.315 (a)(1).",
                listing.getErrorMessages().iterator().next());
    }

    @Test
    public void review_associatesNotAllowedCriteria_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.measure.associatedCriterionNotAllowed"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The %s measure %s for %s cannot have associated criterion %s.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3), i.getArgument(4)));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        Set<CertificationCriterion> associatedCriterion = new LinkedHashSet<CertificationCriterion>();
        associatedCriterion.add(CertificationCriterion.builder()
                .id(1L)
                .number("170.315 (a)(1)")
                .build());
        associatedCriterion.add(CertificationCriterion.builder()
                .id(2L)
                .number("170.315 (a)(2)")
                .build());
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .requiresCriteriaSelection(false)
                        .allowedCriteria(buildCriterionSet(2L, "170.315 (a)(2)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(associatedCriterion)
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertEquals("The G1 measure Test for T cannot have associated criterion 170.315 (a)(1).",
                listing.getErrorMessages().iterator().next());
    }

    @Test
    public void review_listingNotIcsMeasureRemoved_errorMessage() throws ParseException {
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.removedMeasureNoIcs"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format("The %s Measure: %s for %s may not be referenced since "
                        + "this listing does not have ICS. The measure has been removed.",
                        i.getArgument(1), i.getArgument(2), i.getArgument(3)));

        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.setIcs(false);
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .abbreviation("T")
                        .removed(true)
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());

        reviewer.review(listing);

        assertEquals(1, listing.getErrorMessages().size());
        assertEquals("The G1 Measure: Test for T may not be referenced since this listing does not "
                + "have ICS. The measure has been removed.", listing.getErrorMessages().iterator().next());
    }

    @Test
    public void review_listingHasValidMeasures_noErrorMessage() throws ParseException {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertificationResultDTO g1Result = PendingCertificationResultDTO.builder()
                .id(1L)
                .criterion(CertificationCriterionDTO.builder()
                        .id(1L)
                        .number("170.315 (g)(1)")
                        .build())
                .meetsCriteria(true)
                .build();
        listing.getCertificationCriterion().add(g1Result);
        listing.getMeasures().add(PendingCertifiedProductMeasureDTO.builder()
                .measure(Measure.builder()
                        .id(1L)
                        .name("Test")
                        .allowedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                        .build())
                .measureType(MeasureType.builder()
                        .id(1L)
                        .name("G1")
                        .build())
                .associatedCriteria(buildCriterionSet(1L, "170.315 (a)(1)"))
                .build());
        reviewer.review(listing);

        assertEquals(0, listing.getErrorMessages().size());
    }

    private Set<CertificationCriterion> buildCriterionSet(Long id, String number) {
        Set<CertificationCriterion> critSet = new LinkedHashSet<CertificationCriterion>();
        critSet.add(CertificationCriterion.builder()
                .id(id)
                .number(number)
                .build());
        return critSet;
    }
}
