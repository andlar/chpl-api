package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.UserActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityDetails;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.manager.AnnouncementManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "activity")
@RestController
@RequestMapping("/activity")
public class ActivityController {
    private static final Logger LOGGER = LogManager.getLogger(ActivityController.class);
    /** Default value for maximum range of activity to return. */
    public static final int DEFAULT_MAX_ACTIVITY_RANGE_DAYS = 30;

    @Autowired
    private Environment env;
    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private ActivityMetadataManager activityMetadataManager;
    @Autowired
    private AnnouncementManager announcementManager;
    @Autowired
    private TestingLabManager atlManager;
    @Autowired
    private CertifiedProductManager cpManager;
    @Autowired
    private PendingCertifiedProductManager pcpManager;
    @Autowired
    private DeveloperManager developerManager;
    @Autowired
    private ProductManager productManager;
    @Autowired
    private ProductVersionManager versionManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;
    @Autowired
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    @Autowired
    private ResourcePermissions resourcePermissions;

    @Autowired private MessageSource messageSource;

    @ApiOperation(value = "Get detailed audit data for a specific activity event.",
            notes = "Security Restrictions: ROLE_ADMIN and ROLE_ONC may view any activity event. "
                + "Other users may be restricted in what they can see.")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ActivityDetails activityById(@PathVariable("id") final Long id)
                    throws EntityRetrievalException, JsonParseException, IOException, ValidationException {
        ActivityDetails details = activityManager.getActivityById(id);
        return details;
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for listings.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/metadata/listings", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListings(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific listing.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/listings/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        cpManager.getById(id); // throws 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific listing given its "
            + "new-style CHPL product number.",
            notes = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} "
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "activity/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD. "
                    + "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/listings/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                dtos.get(0).getId(), ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific listing given its "
            + "legacy CHPL Number.",
            notes = "{chplPrefix}-{identifier} represents a valid CHPL Product Number.  "
                    + "A valid call to this service "
                    + "would look like activity/certified_products/CHP-999999. "
                    + "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/listings/{chplPrefix}-{identifier}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForListingByChplProductNumber(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                dtos.get(0).getId(), ActivityConcept.CERTIFIED_PRODUCT, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for developers.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/metadata/developers", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForDevelopers(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.DEVELOPER, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific developer.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/developers/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForDeveloperById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        developerManager.getById(id, true); // allows getting activity for deleted developer

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.DEVELOPER, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for products.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/metadata/products", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForProducts(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.PRODUCT, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific product.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/products/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForProductById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        productManager.getById(id, true); //allows getting activity for deleted product

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.PRODUCT, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for version.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/metadata/versions", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForVersions(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityMetadataManager.getActivityMetadataByConcept(
                ActivityConcept.VERSION, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific version.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/versions/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForVersionById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        versionManager.getById(id, true); //allows getting activity for deleted version

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getActivityMetadataByObject(
                id, ActivityConcept.VERSION, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for certification bodies.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all certification bodies.  "
                    + "ROLE_ACB can see activity for their own ACBs.")
    @RequestMapping(value = "/metadata/acbs", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAcbs(@RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, ValidationException {

        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }
        return activityMetadataManager.getCertificationBodyActivityMetadata(startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific certification body.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/acbs/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAcbById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getCertificationBodyActivityMetadata(
                id, startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for testing labs.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all testing labs.  "
                    + "ROLE_ATL can see activity for their own ATLs.")
    @RequestMapping(value = "/metadata/atls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAtls(@RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, ValidationException {

        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }
        return activityMetadataManager.getTestingLabActivityMetadata(startDate, endDate);
    }

    @ApiOperation(value = "Get metadata about auditable records in the system for a specific testing lab.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/metadata/atls/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityMetadata> metadataForAtlById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDates(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingStartHasEnd"));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(msgUtil.getMessage("activity.missingEndHasStart"));
        }

        return activityMetadataManager.getTestingLabActivityMetadata(
                id, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for certification bodies.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all certification bodies.  "
                    + "ROLE_ACB can see their own information.")
    @RequestMapping(value = "/acbs", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForACBs(@RequestParam final Long start,
            @RequestParam final Long end)
                    throws JsonParseException, IOException, ValidationException {

        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return activityManager.getAllAcbActivity(startDate, endDate);
        }
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        return activityManager.getAcbActivity(allowedAcbs, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific certification body.",
            notes = "A start and end date may optionally be provided to limit activity results.  "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all certification bodies.  "
                    + "ROLE_ACB can see their own information.")
    @RequestMapping(value = "/acbs/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")

    public List<ActivityDetails> activityForACBById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        CertificationBodyDTO acb = resourcePermissions.getAcbIfPermissionById(id); // throws 404 if ACB doesn't exist
        if (acb != null && acb.isRetired() && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            LOGGER.warn("Non-admin user " + Util.getUsername()
            + " tried to see activity for retired ACB " + acb.getName());
            throw new AccessDeniedException("Only Admins can see retired ACBs.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        acb = null;
        for (CertificationBodyDTO allowedAcb : allowedAcbs) {
            if (allowedAcb.getId().longValue() == id.longValue()) {
                acb = allowedAcb;
            }
        }
        if (acb == null) {
            String err = String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("acb.accessDenied"),
                    LocaleContextHolder.getLocale()), Util.getUsername(), id);
            throw new AccessDeniedException(err);
        }
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        acbs.add(acb);
        return activityManager.getAcbActivity(acbs, startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for all announcements",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date "
                    + "range of the results. Anonymous users will only receive activity for public "
                    + "announcements.")
    @RequestMapping(value = "/announcements", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForAnnoucements(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return getActivityEventsForAnnouncements(startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for a specific announcement",
            notes = "A start and end date may optionally be provided to limit activity results.  "
                    + "Security Restrictions: Anonymous users are only allowed to see activity for public "
                    + "announcements.  All other roles can see private and public announcements.")
    @RequestMapping(value = "/announcements/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForAnnouncementById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        announcementManager.getById(id, true); // throws 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        return getActivityEventsForAnnouncement(id, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for testing labs.",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all testing labs.  "
                    + "ROLE_ATL can see their own information.")
    @RequestMapping(value = "/atls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityforATLs(@RequestParam final Long start, @RequestParam final Long end)
            throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return activityManager.getAllAtlActivity(startDate, endDate);
        }
        List<TestingLabDTO> allowedAtls = atlManager.getAllForUser();
        return activityManager.getAtlActivity(allowedAtls, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific testing lab.",
            notes = "A start and end date may optionally be provided to limit activity results.  "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC may see activity for all testing labs.  "
                    + "ROLE_ATL can see their own information.")
    @RequestMapping(value = "/atls/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForATLById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        TestingLabDTO atl = resourcePermissions.getAtlIfPermissionById(id); // throws 404 if bad id
        if (atl != null && atl.isRetired() && !resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            LOGGER.warn("Non-admin user " + Util.getUsername()
            + " tried to see activity for retired ATL " + atl.getName());
            throw new AccessDeniedException("Only Admins can see retired ATLs.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        List<TestingLabDTO> allowedAtls = atlManager.getAllForUser();
        atl = null;
        for (TestingLabDTO allowedAtl : allowedAtls) {
            if (allowedAtl.getId().longValue() == id.longValue()) {
                atl = allowedAtl;
            }
        }
        if (atl == null) {
            throw new AccessDeniedException("User " + Util.getUsername() + " does not have access to "
                    + "activity for testing lab with ID " + id);
        }
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();
        atls.add(atl);
        return activityManager.getAtlActivity(atls, startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data for all API keys",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: Only ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/api_keys", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForApiKeys(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityManager.getApiKeyActivity(startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for certified products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/certified_products", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForCertifiedProducts(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return getActivityEventsForCertifiedProducts(startDate, endDate);
    }

    //this is left undeprecated intentionally because
    //the UI uses it on the details page to load the "eye" data
    @ApiOperation(value = "Get auditable data for a specific certified product.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/certified_products/{id:^-?\\d+$}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForCertifiedProductById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start, @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        cpManager.getById(id); // throws 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        return getActivityEventsForCertifiedProductsByIdAndDateRange(id, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED Get auditable data for a specific certified product based on CHPL Product Number.",
            notes = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} "
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "activity/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD. "
                    + "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/certified_products/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForCertifiedProductByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        return getActivityEventsForCertifiedProductsByIdAndDateRange(dtos.get(0).getId(), startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific certified product based on a legacy CHPL Product Number.",
            notes = "{chplPrefix}-{identifier} represents a valid CHPL Product Number.  "
                    + "A valid call to this service "
                    + "would look like activity/certified_products/CHP-999999. "
                    + "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/certified_products/{chplPrefix}-{identifier}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForCertifiedProductByChplProductNumber(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        List<CertifiedProductDetailsDTO> dtos =
                certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        if (dtos.size() == 0) {
            throw new EntityRetrievalException("Could not retrieve CertifiedProductSearchDetails.");
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        return getActivityEventsForCertifiedProductsByIdAndDateRange(dtos.get(0).getId(), startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for all pending certified products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB (specific to own ACB).")
    @RequestMapping(value = "/pending_certified_products", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForPendingCertifiedProducts(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return activityManager.getAllPendingListingActivity(startDate, endDate);
        }
        List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
        return activityManager.getPendingListingActivityByAcb(allowedAcbs, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific pending certified product.",
            notes = "A start and end date may optionally be provided to limit activity results.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB (specific to own ACB).")
    @RequestMapping(value = "/pending_certified_products/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForPendingCertifiedProductById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        //pcpManager will return 404 if the user is not allowed to access it b/c of acb permissions
        //using the "getByIdForActivity" call so ROLE_ONC has access to the pending listing activity.
        //Normally they are not able to see anything else regarding pending listings.
        PendingCertifiedProductDetails pendingListing = pcpManager.getByIdForActivity(id);
        if (pendingListing == null) {
            throw new EntityNotFoundException(msgUtil.getMessage("pendingListing.notFound"));
        } else {
            //make sure the user has permissions on the pending listings acb
            //will throw access denied if they do not have the permissions
            Long pendingListingAcbId =
                    new Long(pendingListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
            resourcePermissions.getAcbIfPermissionById(pendingListingAcbId);
        }

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        //admin can get anything
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return activityManager.getPendingListingActivity(id, startDate, endDate);
        }
        return activityManager.getPendingListingActivity(pendingListing.getId(), startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for all products",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/products", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForProducts(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return getActivityEventsForProducts(startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific product.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/products/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForProducts(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        productManager.getById(id); // returns 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }
        return getActivityEventsForProducts(id, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for all versions",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/versions", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForVersions(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return getActivityEventsForVersions(startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific version.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/versions/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForVersions(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        versionManager.getById(id); // returns 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }
        return getActivityEventsForVersions(id, startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data about all CHPL user accounts",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_CMS_STAFF "
                    + "(of ROLE_CMS_STAFF Users), ROLE_ACB (of their own), or ROLE_ATL (of their own).")
    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @PreAuthorize("isAuthenticated()")
    public List<ActivityDetails> activityForUsers(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return activityManager.getAllUserActivity(startDate, endDate);
        }
        Set<Long> userIdsToSearch = getAllowedUsersForActivitySearch();
        return activityManager.getUserActivity(userIdsToSearch, startDate, endDate);
    }

    @ApiOperation(value = "Get auditable data about a specific CHPL user account.",
            notes = "A start and end date may optionally be provided to limit activity results.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_CMS_STAFF "
                    + "(of ROLE_CMS_STAFF Users), ROLE_ACB (of their own), or ROLE_ATL (of their own).")
    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForUsers(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        userManager.getById(id); // throws 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }

        //ROLE_ADMIN can get user activity on any user; other roles must
        //have some association to the user so check if this request is allowed.
        if (!resourcePermissions.isUserRoleAdmin() && !resourcePermissions.isUserRoleOnc()) {
            Set<Long> allowedUserIds = getAllowedUsersForActivitySearch();
            if (!allowedUserIds.contains(id)) {
                throw new AccessDeniedException("User " + Util.getUsername()
                + " does not have permission to get activity for user with ID " + id);
            }
        }
        Set<Long> userIdsToSearch = new HashSet<Long>();
        userIdsToSearch.add(id);
        return activityManager.getUserActivity(userIdsToSearch, startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data about all developers",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results.")
    @RequestMapping(value = "/developers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForDevelopers(@RequestParam final Long start,
            @RequestParam final Long end) throws JsonParseException, IOException, ValidationException {

        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return getActivityEventsForDevelopers(startDate, endDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get auditable data for a specific developer.",
            notes = "A start and end date may optionally be provided to limit activity results.")
    @RequestMapping(value = "/developers/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityForDeveloperById(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, EntityRetrievalException, ValidationException {
        developerManager.getById(id); // returns 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }
        return getActivityEventsForDevelopers(id, startDate, endDate);
    }

    @ApiOperation(value = "Track the actions of all users in the system",
            notes = "Users must specify 'start' and 'end' parameters to restrict the date range of the results."
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/user_activities", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<UserActivity> activityByUser(@RequestParam final Long start,
            @RequestParam final Long end)
                    throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        validateActivityDatesAndDateRange(start, end);
        return activityManager.getActivityByUserInDateRange(startDate, endDate);
    }

    @ApiOperation(value = "Track the actions of a specific user in the system",
            notes = "A start and end date may optionally be provided to limit activity results."
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/user_activities/{id}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public List<ActivityDetails> activityByUser(@PathVariable("id") final Long id,
            @RequestParam(required = false) final Long start,
            @RequestParam(required = false) final Long end)
                    throws JsonParseException, IOException, UserRetrievalException, ValidationException {
        userManager.getById(id); // throws 404 if bad id

        //if one of start of end is provided then the other must also be provided.
        //if neither is provided then query all dates
        Date startDate = new Date(0);
        Date endDate = new Date();
        if (start != null && end != null) {
            validateActivityDatesAndDateRange(start, end);
            startDate = new Date(start);
            endDate = new Date(end);
        } else if (start == null && end != null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingStartHasEnd"),
                    LocaleContextHolder.getLocale()));
        } else if (start != null && end == null) {
            throw new IllegalArgumentException(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("activity.missingEndHasStart"),
                    LocaleContextHolder.getLocale()));
        }
        return activityManager.getActivityForUserInDateRange(id, startDate, endDate);
    }

    private Set<Long> getAllowedUsersForActivitySearch() {
        Set<Long> allowedUserIds = new HashSet<Long>();
        //user can see their own activity
        allowedUserIds.add(Util.getCurrentUser().getId());

        //user can see activity for other users in the same acb

        if (resourcePermissions.isUserRoleAcbAdmin()) {
            List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
            for (CertificationBodyDTO acb : allowedAcbs) {
                List<UserDTO> acbUsers = resourcePermissions.getAllUsersOnAcb(acb);
                for (UserDTO user : acbUsers) {
                    allowedUserIds.add(user.getId());
                }
            }
        }
        //user can see activity for other users in the same atl
        if (resourcePermissions.isUserRoleAtlAdmin()) {
            List<TestingLabDTO> allowedAtls = atlManager.getAllForUser();
            for (TestingLabDTO atl : allowedAtls) {
                List<UserDTO> atlUsers = resourcePermissions.getAllUsersOnAtl(atl);
                for (UserDTO user : atlUsers) {
                    allowedUserIds.add(user.getId());
                }
            }
        }
        //user can see activity for other users with role cms_staff
        if (resourcePermissions.isUserRoleCmsStaff()) {
            List<UserDTO> cmsStaffUsers = userManager.getUsersWithPermission("ROLE_CMS_STAFF");
            for (UserDTO user : cmsStaffUsers) {
                allowedUserIds.add(user.getId());
            }
        }
        return allowedUserIds;
    }

    private List<ActivityDetails> getActivityEventsForCertifiedProductsByIdAndDateRange(final Long id,
            final Date startDate, final Date endDate)
                    throws JsonParseException, IOException {

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityDetails> getActivityEventsForProducts(final Long id, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.PRODUCT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityDetails> getActivityEventsForDevelopers(final Long id, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.DEVELOPER;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;
    }

    private List<ActivityDetails> getActivityEventsForVersions(final Long id, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.VERSION;
        events = getActivityEventsForObject(concept, id, startDate, endDate);
        return events;

    }

    private List<ActivityDetails> getActivityEventsForCertifiedProducts(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested certified product activity between " + startDate
                + " and " + endDate);

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.CERTIFIED_PRODUCT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityDetails> getActivityEventsForProducts(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info(
                "User " + Util.getUsername() + " requested product activity between " + startDate + " and " + endDate);

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.PRODUCT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityDetails> getActivityEventsForDevelopers(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info("User " + Util.getUsername() + " requested developer activity between " + startDate + " and "
                + endDate);

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.DEVELOPER;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityDetails> getActivityEventsForVersions(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        LOGGER.info(
                "User " + Util.getUsername() + " requested version activity between " + startDate + " and " + endDate);

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.VERSION;
        events = getActivityEventsForConcept(concept, startDate, endDate);
        return events;

    }

    private List<ActivityDetails> getActivityEventsForAnnouncements(final Date startDate, final Date endDate)
            throws JsonParseException, IOException {
        if (Util.getCurrentUser() == null) {
            LOGGER.info("Anonymous user requested public announcement activity between " + startDate + " and "
                    + endDate);
            return activityManager.getPublicAnnouncementActivity(startDate, endDate);
        }

        LOGGER.info("User " + Util.getUsername() + " requested all announcement activity between " + startDate + " and "
                + endDate);
        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.ANNOUNCEMENT;
        events = getActivityEventsForConcept(concept, startDate, endDate);

        return events;
    }

    private List<ActivityDetails> getActivityEventsForAnnouncement(final Long id,
            final Date startDate, final Date endDate)
                    throws JsonParseException, IOException {
        if (Util.getCurrentUser() == null) {
            LOGGER.info("Anonymous user requested public announcement activity "
                    + "for announcement id " + id + " between " + startDate + " and "
                    + endDate);
            return activityManager.getPublicAnnouncementActivity(id, startDate, endDate);
        }

        LOGGER.info("User " + Util.getUsername() + " requested all announcement activity "
                + "for announcement id " + id + " between " + startDate + " and "
                + endDate);

        List<ActivityDetails> events = null;
        ActivityConcept concept = ActivityConcept.ANNOUNCEMENT;
        events = getActivityEventsForObject(concept, id, startDate, endDate);

        return events;
    }

    private List<ActivityDetails> getActivityEventsForConcept(final ActivityConcept concept,
            final Date startDate, final Date endDate) throws JsonParseException, IOException {
        return activityManager.getActivityForConcept(concept, startDate, endDate);
    }

    private List<ActivityDetails> getActivityEventsForObject(final ActivityConcept concept, final Long objectId,
            final Date startDate, final Date endDate) throws JsonParseException, IOException {
        return activityManager.getActivityForObject(concept, objectId, startDate, endDate);
    }

    /**
     * Make sure the start date is not after the end date.
     * @param startDate
     * @param endDate
     * @throws IllegalArgumentException
     */
    private void validateActivityDates(final Long startDate, final Long endDate) throws IllegalArgumentException {
        LocalDate startDateUtc =
                Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc =
                Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDateUtc.isAfter(endDateUtc)) {
            throw new IllegalArgumentException("Cannot search for activity with the start date after the end date");
        }
    }

    /**
     * Validates the start is not after the end date.
     * Validates the amount of time between start/end dates is not larger than our defined max range.
     * @param startDate
     * @param endDate
     * @throws IllegalArgumentException
     */
    private void validateActivityDatesAndDateRange(final Long startDate, final Long endDate) throws IllegalArgumentException {
        LocalDate startDateUtc =
                Instant.ofEpochMilli(startDate).atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDateUtc =
                Instant.ofEpochMilli(endDate).atZone(ZoneId.of("UTC")).toLocalDate();

        if (startDateUtc.isAfter(endDateUtc)) {
            throw new IllegalArgumentException("Cannot search for activity with the start date after the end date");
        }

        Integer maxActivityRangeInDays = Integer.getInteger(
                env.getProperty("maxActivityRangeInDays"), DEFAULT_MAX_ACTIVITY_RANGE_DAYS);
        endDateUtc = endDateUtc.minusDays(maxActivityRangeInDays);
        if (startDateUtc.isBefore(endDateUtc)) {
            throw new IllegalArgumentException(
                    "Cannot search for activity with a date range more than " + maxActivityRangeInDays + " days.");
        }
    }
}
