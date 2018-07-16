package gov.healthit.chpl.app.chartdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

/**
 * Retrieves all of the 2015 SED Products and their details.  Details are retrieved asynchronously according
 * to the chartDataExecutor defined in AppConfig.
 * @author TYoung
 *
 */
public class SedDataCollector {
    private ChartDataApplicationEnvironment appEnvironment;
    private static final Logger LOGGER = LogManager.getLogger(SedDataCollector.class);
    private static final String EDITION_2015 = "2015";
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    SedDataCollector(final ChartDataApplicationEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
        initialize();
    }

    /**
     * This method runs the data retrieval process for the 2015 SED products and their details.
     * @param listings initial set of Listings
     * @return List of CertifiedProductSearchDetails
     */
    public List<CertifiedProductSearchDetails> retreiveData(final List<CertifiedProductFlatSearchResult> listings) {

        List<CertifiedProductFlatSearchResult> certifiedProducts = filterData(listings);
        LOGGER.info("2015/SED Certified Product Count: " + certifiedProducts.size());

        List<CertifiedProductSearchDetails> certifiedProductsWithDetails = getCertifiedProductDetailsForAll(
                certifiedProducts);

        return certifiedProductsWithDetails;
    }

    private List<CertifiedProductFlatSearchResult> filterData(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>();
        for (CertifiedProductFlatSearchResult result : certifiedProducts) {
            if (result.getEdition().equalsIgnoreCase(EDITION_2015)
                    && result.getCriteriaMet().contains("170.315 (g)(3)")) {
                results.add(result);
            }
        }
        return results;
    }

    private List<CertifiedProductSearchDetails> getCertifiedProductDetailsForAll(
            final List<CertifiedProductFlatSearchResult> certifiedProducts) {

        List<CertifiedProductSearchDetails> details = new ArrayList<CertifiedProductSearchDetails>();
        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        DataCollectorAsyncHelper dataCollectorAsyncHelper =
                (DataCollectorAsyncHelper) appEnvironment.getSpringManagedObject("dataCollectorAsyncHelper");

        for (CertifiedProductFlatSearchResult certifiedProduct : certifiedProducts) {
            try {
                futures.add(dataCollectorAsyncHelper
                        .getCertifiedProductDetail(certifiedProduct.getId(), certifiedProductDetailsManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + certifiedProduct.getId(), e);
            }
        }

        Date startTime = new Date();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                details.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Could not retrieve certified product details for unknown id.", e);
            }
        }

        Date endTime = new Date();
        LOGGER.info("Time to retrieve details: " + (endTime.getTime() - startTime.getTime()));

        return details;
    }

    private void initialize() {
        certifiedProductDetailsManager = (CertifiedProductDetailsManager) appEnvironment
                .getSpringManagedObject("certifiedProductDetailsManager");
    }
}
