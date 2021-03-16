package gov.healthit.chpl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.HttpStatusAwareCache;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertificationStatusEventComparator;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import lombok.NoArgsConstructor;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import one.util.streamex.StreamEx;

@Component("directReviewSearchService")
@NoArgsConstructor
public class DirectReviewSearchService {
    private DirectReviewCachingService drCacheService;

    @Autowired
    public DirectReviewSearchService(DirectReviewCachingService drCacheService) {
        this.drCacheService = drCacheService;
    }

    public boolean getDirectReviewsAvailable() {
        boolean directReviewsAvailable = false;
        Ehcache drCache = getDirectReviewsCache();
        if (drCache instanceof HttpStatusAwareCache) {
            HttpStatusAwareCache drStatusAwareCache = (HttpStatusAwareCache) drCache;
            directReviewsAvailable = drStatusAwareCache.getHttpStatus() != null
                    && drStatusAwareCache.getHttpStatus().is2xxSuccessful();
        }
        return directReviewsAvailable;
    }

    public List<DirectReview> getAll() {
        List<DirectReview> drs = new ArrayList<DirectReview>();

        Ehcache drCache = getDirectReviewsCache();
        drs = drCache.getAll(drCache.getKeys()).values().stream()
            .map(value -> value.getObjectValue())
            .filter(objValue -> objValue != null && (objValue instanceof List<?>))
            .map(objValue -> ((List<?>) objValue))
            .filter(listValue -> listValue.size() > 0)
            .flatMap(listValue -> listValue.stream())
            .map(listItemAsObject -> (DirectReview) listItemAsObject)
            .collect(Collectors.toList());

        return drs;
    }

    public List<DirectReview> getDeveloperDirectReviews(Long developerId) {
        return drCacheService.getDeveloperDirectReviewsFromCache(developerId);
    }

    /**
     * The set of direct reviews related to the listing includes those that have a developer-associated listing
     * with the same listing ID during a time when that listing was Active
     * AND those that are for the listing's developer but do not have any
     * developer-associated listings if the listing is 2015+ edition.
     */
    public List<DirectReview> getDirectReviewsRelatedToListing(CertifiedProductSearchDetails listing) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        drs.addAll(getDirectReviewsWithDeveloperAssociatedListing(listing));

        String editionYear = MapUtils.getString(listing.getCertificationEdition(), CertifiedProductSearchDetails.EDITION_NAME_KEY);
        if (!StringUtils.isEmpty(editionYear) && editionYear.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            Long developerId = listing.getDeveloper().getDeveloperId();
            drs.addAll(getDeveloperDirectReviewsWithoutAssociatedListings(developerId));
        }

        drs = StreamEx.of(drs)
            .distinct(DirectReview::getJiraKey)
            .collect(Collectors.toList());

        return drs;
    }

    public List<DirectReview> getDirectReviewsRelatedToListing(Long listingId, Long developerId) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        drs.addAll(getDirectReviewsWithDeveloperAssociatedListingId(listingId));
        drs.addAll(getDeveloperDirectReviewsWithoutAssociatedListings(developerId));

        drs = StreamEx.of(drs)
            .distinct(DirectReview::getJiraKey)
            .collect(Collectors.toList());

        return drs;
    }

    private List<DirectReview> getDeveloperDirectReviewsWithoutAssociatedListings(Long developerId) {
        List<DirectReview> allDeveloperDirectReviews = getDeveloperDirectReviews(developerId);
        List<DirectReview> drsWithoutAssociatedListings = Stream.of(
                allDeveloperDirectReviews.stream()
                .filter(dr -> dr.getNonConformities() == null || dr.getNonConformities().size() == 0)
                .collect(Collectors.toList()),
                allDeveloperDirectReviews.stream()
                .filter(dr -> hasNoDeveloperAssociatedListings(dr.getNonConformities()))
                .collect(Collectors.toList()))
          .flatMap(List::stream)
          .collect(Collectors.toList());
        return drsWithoutAssociatedListings;
    }

    private boolean hasNoDeveloperAssociatedListings(List<DirectReviewNonConformity> ncs) {
        return ncs.stream()
            .filter(nc -> nc.getDeveloperAssociatedListings() == null || nc.getDeveloperAssociatedListings().size() == 0)
            .findAny()
            .isPresent();
    }

    private List<DirectReview> getDirectReviewsWithDeveloperAssociatedListing(CertifiedProductSearchDetails listing) {
        List<DirectReview> allDirectReviews = getAll();
        return allDirectReviews.stream()
                .filter(dr -> isAssociatedWithListing(dr, listing.getId()))
                .filter(dr -> isOpenWhileListingIsActive(dr, listing))
                .collect(Collectors.toList());
    }

    private List<DirectReview> getDirectReviewsWithDeveloperAssociatedListingId(Long listingId) {
        List<DirectReview> allDirectReviews = getAll();
        return allDirectReviews.stream()
                .filter(dr -> isAssociatedWithListing(dr, listingId))
                .collect(Collectors.toList());
    }

    private boolean isAssociatedWithListing(DirectReview dr, Long listingId) {
        if (dr.getNonConformities() == null || dr.getNonConformities().size() == 0) {
            return false;
        }

        return dr.getNonConformities().stream()
            .filter(nc -> nc.getDeveloperAssociatedListings() != null && nc.getDeveloperAssociatedListings().size() > 0)
            .flatMap(nc -> nc.getDeveloperAssociatedListings().stream())
            .filter(devAssocListing -> devAssocListing.getId().equals(listingId))
            .findAny().isPresent();
    }

    private boolean isOpenWhileListingIsActive(DirectReview dr, CertifiedProductSearchDetails listing) {
        if (dr.getStartDate() == null) {
            return false;
        }
        Date drStartDate = dr.getStartDate();

        List<DateRange> activeDateRanges = getDateRangesWithActiveStatus(listing);
        activeDateRanges.stream()
            .filter(activeDates -> activeDates.getUpperMillis() >= drStartDate.getTime())
            .findAny().isPresent();
        return true;
    }

    private List<DateRange> getDateRangesWithActiveStatus(CertifiedProductSearchDetails listing) {
        List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
        List<CertificationStatusEvent> listingStatusEvents = listing.getCertificationEvents();
        listingStatusEvents.sort(new CertificationStatusEventComparator());
        return IntStream.range(0, listingStatusEvents.size())
            .filter(i -> listingStatusEvents.get(i) != null && listingStatusEvents.get(i).getStatus() != null
                && !StringUtils.isEmpty(listingStatusEvents.get(i).getStatus().getName()))
            .filter(i -> activeStatuses.contains(listingStatusEvents.get(i).getStatus().getName()))
            .mapToObj(i -> new DateRange(new Date(listingStatusEvents.get(i).getEventDate()),
                    i < (listingStatusEvents.size() - 1) ? new Date() : new Date(listingStatusEvents.get(i + 1).getEventDate())))
            .collect(Collectors.toList());
    }

    private Ehcache getDirectReviewsCache() {
        CacheManager manager = CacheManager.getInstance();
        Ehcache drCache = manager.getEhcache(CacheNames.DIRECT_REVIEWS);
        return drCache;
    }
}
