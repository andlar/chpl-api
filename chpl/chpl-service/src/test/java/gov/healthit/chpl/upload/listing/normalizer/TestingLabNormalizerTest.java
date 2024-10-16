package gov.healthit.chpl.upload.listing.normalizer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class TestingLabNormalizerTest {

    private TestingLabDAO atlDao;
    private TestingLabNormalizer normalizer;
    private ErrorMessageUtil errorMessageUtil;

    @Before
    public void setup() {
        atlDao = Mockito.mock(TestingLabDAO.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn("Error Message");

        normalizer = new TestingLabNormalizer(atlDao, new ChplProductNumberUtil(), new ValidationUtils(), errorMessageUtil);
    }

    @Test
    public void normalize_nullTestingLabs_noChanges() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setTestingLabs(null);
        normalizer.normalize(listing);
        assertNull(listing.getTestingLabs());
    }

    @Test
    public void normalize_emptyTestingLabs_noChanges() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(new ArrayList<CertifiedProductTestingLab>())
                .build();
        normalizer.normalize(listing);
        assertNotNull(listing.getTestingLabs());
        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void normalize_testingLabIdNameCodeExist_noLookup() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                    .testingLab(TestingLab.builder()
                            .id(1L)
                            .name("ICSA")
                            .atlCode("TL")
                            .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .testingLabs(atls)
                .build();
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("TL", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabIdNameCodeExistLegacyChplProductNumber_noLookup() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("ICSA")
                        .atlCode("TL")
                        .build())
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .testingLabs(atls)
                .build();
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("TL", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabCodeMissing_lookupById() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("ICSA")
                        .build())
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(TestingLab.builder()
                .id(1L)
                .name("ICSA")
                .atlCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabNameMissing_lookupById() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .atlCode("TL")
                        .build())
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenReturn(TestingLab.builder()
                .id(1L)
                .name("ICSA")
                .atlCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabCodeMissing_lookupByIdNotFound() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(1L)
                        .name("ICSA")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getById(ArgumentMatchers.anyLong()))
        .thenThrow(EntityRetrievalException.class);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertNull(listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabIdMissing_lookupByName() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(null)
                        .name("ICSA")
                        .atlCode("TL")
                        .build())
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(TestingLab.builder()
                .id(1L)
                .name("ICSA")
                .atlCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_testingLabIdMissing_lookupByNameNotFound() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
            .testingLab(TestingLab.builder()
                    .id(null)
                    .name("ICSA")
                    .atlCode("01")
                    .build())
            .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByName(ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertNull(listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    public void normalize_testingLabIdAndNameMissing_lookupByCode() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(null)
                        .name("")
                        .atlCode("01")
                        .build())
                .build());
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(TestingLab.builder()
                .id(1L)
                .name("ICSA")
                .atlCode("01")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    public void normalize_testingLabIdAndNameMissing_lookupByCodeNotFound() {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        atls.add(CertifiedProductTestingLab.builder()
                .testingLab(TestingLab.builder()
                        .id(null)
                        .name("")
                        .atlCode("01")
                        .build())
                .build());

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertNull(listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("01", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_atlsEmptyFindByChplProductNumber_lookupByCodeFound() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(TestingLab.builder()
                .id(1L)
                .name("ICSA")
                .atlCode("07")
                .build());
        normalizer.normalize(listing);

        assertEquals(1, listing.getTestingLabs().size());
        assertEquals(1L, listing.getTestingLabs().get(0).getTestingLab().getId());
        assertEquals("ICSA", listing.getTestingLabs().get(0).getTestingLab().getName());
        assertEquals("07", listing.getTestingLabs().get(0).getTestingLab().getAtlCode());
    }

    @Test
    public void normalize_atlsEmptyFindByChplProductNumber_lookupByCodeNotFound() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.07.04.2663.ABCD.R2.01.0.200511")
                .testingLabs(atls)
                .build();

        Mockito.when(atlDao.getByCode(ArgumentMatchers.anyString()))
        .thenReturn(null);
        normalizer.normalize(listing);

        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void normalize_atlsEmptyAtlCodeEmpty_noLookup() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15..04.2663.ABCD.R2.01.0.200511")
                .testingLabs(atls)
                .build();

        normalizer.normalize(listing);

        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void normalize_atlsEmptyAtlCodeInvalidFormat_noLookup() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.T?.04.2663.ABCD.R2.01.0.200511")
                .testingLabs(atls)
                .build();

        normalizer.normalize(listing);

        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void normalize_atlsEmptyLegacyChplProductNumber_noLookup() throws EntityRetrievalException {
        List<CertifiedProductTestingLab> atls = new ArrayList<CertifiedProductTestingLab>();
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("CHP-123456")
                .testingLabs(atls)
                .build();

        normalizer.normalize(listing);

        assertEquals(0, listing.getTestingLabs().size());
    }
}
