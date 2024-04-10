package gov.healthit.chpl.developer.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.search.domain.SearchSetOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperSearchRequest implements Serializable {
    private static final long serialVersionUID = 117927209216670161L;
    public static final String DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private String searchTerm;
    private String developerName;
    private String developerCode;

    @Builder.Default
    private Set<String> statuses = new HashSet<String>();

    @Builder.Default
    private Set<String> acbsForActiveListings = new HashSet<String>();
    @Builder.Default
    private Set<String> acbsForAllListings = new HashSet<String>();

    private String decertificationDateStart;
    private String decertificationDateEnd;

    @JsonIgnore
    @Builder.Default
    private Set<String> activeListingsOptionsStrings = new HashSet<String>();
    @Builder.Default
    private Set<ActiveListingSearchOptions> activeListingsOptions = new HashSet<ActiveListingSearchOptions>();
    @JsonIgnore
    private String activeListingsOptionsOperatorString;
    private SearchSetOperator activeListingsOptionsOperator;

    private Boolean hasNotSubmittedAttestationsForMostRecentPastPeriod;
    private Boolean hasSubmittedAttestationsForMostRecentPastPeriod;

    @JsonIgnore
    private String orderByString;
    private OrderByOption orderBy;
    @Builder.Default
    private Boolean sortDescending = false;
    @Builder.Default
    private Integer pageNumber = 0;
    @Builder.Default
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
