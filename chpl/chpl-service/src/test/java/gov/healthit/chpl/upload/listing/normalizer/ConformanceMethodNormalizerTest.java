package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.ConformanceMethodDAO;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;

public class ConformanceMethodNormalizerTest {
    private ConformanceMethodDAO cmDao;
    private ConformanceMethodNormalizer normalizer;

    @Before
    public void before() {
        cmDao = Mockito.mock(ConformanceMethodDAO.class);
        List<ConformanceMethodCriteriaMap> allowedCms = new ArrayList<ConformanceMethodCriteriaMap>();
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("CM 1")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(1L)
                        .number("170.315 (a)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(2L)
                        .name("CM 2")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(2L)
                        .number("170.315 (b)(1)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(1L)
                        .name("CM 1")
                        .build())
                .build());
        allowedCms.add(ConformanceMethodCriteriaMap.builder()
                .criterion(CertificationCriterion.builder()
                        .id(10L)
                        .number("170.315 (c)(2)")
                        .build())
                .conformanceMethod(ConformanceMethod.builder()
                        .id(5L)
                        .name("NCQA eCQM Test Method")
                        .removalDate(LocalDate.parse("2022-06-01"))
                        .build())
                .build());

        try {
            Mockito.when(cmDao.getAllConformanceMethodCriteriaMap()).thenReturn(allowedCms);
        } catch (EntityRetrievalException e) {
        }

        normalizer = new ConformanceMethodNormalizer(cmDao);
    }

    @Test
    public void normalize_emptyConformanceMethodAndCriteriaHasMultipleAllowed_DefaultNotPopulated() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
    }

    @Test
    public void normalize_emptyConformanceMethodAndUnattestedCriteriaHasOneAllowed_NoDefaultPopulated() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .certificationResult(CertificationResult.builder()
                        .success(false)
                        .criterion(CertificationCriterion.builder()
                                .id(2L)
                                .number("170.315 (b)(1)")
                                .build())
                        .conformanceMethods(new ArrayList<CertificationResultConformanceMethod>())
                        .build())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getCertificationResults().get(0).getConformanceMethods());
        assertEquals(0, listing.getCertificationResults().get(0).getConformanceMethods().size());
    }

    @Test
    public void normalize_conformanceMethodNotAllowedForCriterion_idIsNull() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("notindb")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(create2015Edition())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getId());
        assertEquals("notindb", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
    }

    @Test
    public void normalize_conformanceMethodAllowedForCriterion_setsIdField() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("CM 1")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(create2015Edition())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(1L)
                                .number("170.315 (a)(1)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(1L, listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getId());
        assertNull(listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getRemovalDate());
        assertEquals("CM 1", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
        assertEquals("100", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
    }

    @Test
    public void normalize_removedConformanceMethodAllowedForCriterion_setsIdAndRemovalDateFields() {
        List<CertificationResultConformanceMethod> cms = new ArrayList<CertificationResultConformanceMethod>();
        cms.add(CertificationResultConformanceMethod.builder()
                .id(null)
                .conformanceMethod(ConformanceMethod.builder()
                        .id(null)
                        .name("NCQA eCQM Test Method")
                        .build())
                .conformanceMethodVersion("100")
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .edition(create2015Edition())
                .certificationResult(CertificationResult.builder()
                        .success(true)
                        .criterion(CertificationCriterion.builder()
                                .id(10L)
                                .number("170.315 (c)(2)")
                                .build())
                        .conformanceMethods(cms)
                        .build())
                .build();
        normalizer.normalize(listing);
        assertEquals(1, listing.getCertificationResults().get(0).getConformanceMethods().size());
        assertEquals(LocalDate.parse("2022-06-01"), listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getRemovalDate());
        assertEquals("NCQA eCQM Test Method", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethod().getName());
        assertEquals("100", listing.getCertificationResults().get(0).getConformanceMethods().get(0).getConformanceMethodVersion());
    }

    private CertificationEdition create2015Edition() {
        return CertificationEdition.builder()
                .id(3L)
                .name("2015")
                .build();
    }
}
