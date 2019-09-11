package gov.healthit.chpl.changerequest.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.dao.ChangeRequestTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestCertificationBodyMap;
import gov.healthit.chpl.changerequest.domain.ChangeRequestWebsite;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationContext;
import gov.healthit.chpl.changerequest.validation.ChangeRequestValidationFactory;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestManagerImpl extends SecurityManager implements ChangeRequestManager {

    @Value("${changerequest.status.pendingacbaction}")
    private Long pendingAcbActionStatus;

    @Value("${changerequest.status.accepted}")
    private Long acceptedStatus;

    @Value("${changerequest.website}")
    private Long websiteChangeRequestType;

    private ChangeRequestDAO changeRequestDAO;
    private ChangeRequestTypeDAO changeRequestTypeDAO;
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private DeveloperDAO developerDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationBodyDAO certificationBodyDAO;
    private ChangeRequestCertificationBodyMapHelper crCertificationBodyMapHelper;
    private ChangeRequestStatusHelper crStatusHelper;
    private ChangeRequestValidationFactory crValidationFactory;
    private ChangeRequestWebsiteHelper crWebsiteHelper;
    private ActivityManager activityManager;

    @Autowired
    public ChangeRequestManagerImpl(final ChangeRequestDAO changeRequestDAO,
            final ChangeRequestTypeDAO changeRequestTypeDAO,
            final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, final DeveloperDAO developerDAO,
            final CertifiedProductDAO certifiedProductDAO, final CertificationBodyDAO certificationBodyDAO,
            final ChangeRequestCertificationBodyMapHelper changeRequestCertificationBodyMapHelper,
            final ChangeRequestStatusTypeDAO crStatusTypeDAO, final ChangeRequestStatusHelper crStatusHelper,
            final ChangeRequestValidationFactory crValidationFactory,
            final ChangeRequestWebsiteHelper crWebsiteHelper,
            final ActivityManager activityManager) {
        this.changeRequestDAO = changeRequestDAO;
        this.changeRequestTypeDAO = changeRequestTypeDAO;
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.developerDAO = developerDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationBodyDAO = certificationBodyDAO;
        this.crCertificationBodyMapHelper = changeRequestCertificationBodyMapHelper;
        this.crStatusHelper = crStatusHelper;
        this.crValidationFactory = crValidationFactory;
        this.crWebsiteHelper = crWebsiteHelper;
        this.activityManager = activityManager;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).CREATE, #cr)")
    public ChangeRequest createChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runCreateValidations(cr));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        // Save the base change request
        ChangeRequest newCr = createBaseChangeRequest(cr);
        // Save the change request details
        newCr = createChangeRequestDetails(newCr, cr.getDetails());
        // Get the new chnage request as it exists in DB
        newCr = getChangeRequest(newCr.getId());

        activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request created", null,
                newCr);
        return newCr;
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_BY_ID, returnObject)")
    public ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = new ChangeRequest();
        cr = changeRequestDAO.get(changeRequestId);
        return populateChangeRequestData(cr);
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).GET_ALL)")
    public List<ChangeRequest> getAllChangeRequestsForUser() throws EntityRetrievalException {
        List<ChangeRequest> requests = changeRequestDAO.getAllForCurrentUser().stream()
                .map(cr -> {
                    try {
                        return populateChangeRequestData(cr);
                    } catch (EntityRetrievalException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.<ChangeRequest> toList());
        return requests;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CHANGE_REQUEST, "
            + "T(gov.healthit.chpl.permissions.domains.ChangeRequestDomainPermissions).UPDATE, #cr)")
    public ChangeRequest updateChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        ValidationException validationException = new ValidationException();
        validationException.getErrorMessages().addAll(runUpdateValidations(cr));
        if (validationException.getErrorMessages().size() > 0) {
            throw validationException;
        }

        ChangeRequest crFromDb = getChangeRequest(cr.getId());
        crStatusHelper.updateChangeRequestStatus(crFromDb, cr);
        updateChangeRequestDetails(crFromDb, cr.getDetails());
        executeChangeRequest(crFromDb);
        ChangeRequest newCr = getChangeRequest(cr.getId());

        if (!newCr.getCurrentStatus().getChangeRequestStatusType().getId()
                .equals(crFromDb.getCurrentStatus().getChangeRequestStatusType().getId())) {
            activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request status updated",
                    crFromDb, newCr);
        }
        if (didDetailsChange(crFromDb, newCr)) {
            activityManager.addActivity(ActivityConcept.CHANGE_REQUEST, newCr.getId(), "Change request details updated",
                    crFromDb, newCr);
        }
        return newCr;
    }

    private ChangeRequest populateChangeRequestData(final ChangeRequest cr) throws EntityRetrievalException {
        cr.setDetails(getChangeRequestDetails(cr));
        cr.setStatuses(crStatusHelper.getStatuses(cr.getId()));
        cr.setCertificationBodies(
                crCertificationBodyMapHelper.getCertificationBodiesByChangeRequestId(cr.getId()));
        return cr;
    }

    private ChangeRequest createBaseChangeRequest(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequest newCr = changeRequestDAO.create(cr);
        newCr.getStatuses().add(crStatusHelper.saveInitialStatus(newCr));
        createCertificationBodies(newCr).stream()
                .map(crAcbMap -> newCr.getCertificationBodies().add(crAcbMap.getCertificationBody()));
        return newCr;
    }

    private List<ChangeRequestCertificationBodyMap> createCertificationBodies(ChangeRequest cr)
            throws EntityRetrievalException {
        return getCertificationBodiesByDeveloper(cr.getDeveloper()).stream()
                .map(result -> crCertificationBodyMapHelper.saveCertificationBody(cr, result))
                .collect(Collectors.<ChangeRequestCertificationBodyMap> toList());
    }

    private List<CertificationBody> getCertificationBodiesByDeveloper(final Developer developer) {
        Map<Long, CertificationBody> acbs = new HashMap<Long, CertificationBody>();

        certifiedProductDAO.findByDeveloperId(developer.getDeveloperId()).stream()
                .filter(result -> !acbs.containsKey(result.getCertificationBodyId()))
                .forEach(result -> {
                    acbs.put(result.getCertificationBodyId(), getCertificationBody(result.getCertificationBodyId()));
                });
        return new ArrayList<CertificationBody>(acbs.values());
    }

    private CertificationBody getCertificationBody(final Long certificationBodyId) {
        try {
            return new CertificationBody(certificationBodyDAO.getById(certificationBodyId));
        } catch (EntityRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private Object getChangeRequestDetails(ChangeRequest cr) throws EntityRetrievalException {
        if (isWebsiteChangeRequest(cr)) {
            return crWebsiteHelper.getByChangeRequestId(cr.getId());
        } else {
            return null;
        }
    }

    private ChangeRequest createChangeRequestDetails(final ChangeRequest cr, final Object details) {
        // Data in the "details" object is unfortunately a hashmap
        if (isWebsiteChangeRequest(cr)) {
            ChangeRequestWebsite crWebsite = crWebsiteHelper.getDetailsFromHashMap((HashMap<String, Object>) details);
            cr.setDetails(crWebsiteHelper.create(cr, crWebsite));
        }
        return cr;
    }

    private void updateChangeRequestDetails(final ChangeRequest cr, final Object details) {
        // Data in the "details" object is unfortunately a hashmap
        if (isWebsiteChangeRequest(cr)) {
            ChangeRequestWebsite crWebsite = crWebsiteHelper.getDetailsFromHashMap((HashMap<String, Object>) details);
            crWebsiteHelper.update(cr, crWebsite);
        }
    }

    private void executeChangeRequest(final ChangeRequest cr) throws EntityRetrievalException, EntityCreationException {
        if (isChangeRequestAccepted(cr)) {
            if (isWebsiteChangeRequest(cr)) {
                crWebsiteHelper.execute(cr);
            }
        }
    }

    private boolean didDetailsChange(ChangeRequest origChangeRequest, ChangeRequest updatedChangeRequest) {
        if (isWebsiteChangeRequest(origChangeRequest)) {
            return !((ChangeRequestWebsite) origChangeRequest.getDetails())
                    .equals((ChangeRequestWebsite) updatedChangeRequest.getDetails());
        } else {
            return false;
        }
    }

    private List<String> runCreateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_IN_PROCESS));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_EXISTENCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_CREATE));
        return runValidations(rules, cr);
    }

    private List<String> runUpdateValidations(ChangeRequest cr) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_EXISTENCE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.CHANGE_REQUEST_DETAILS_UPDATE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.DEVELOPER_ACTIVE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_TYPE));
        rules.add(crValidationFactory.getRule(ChangeRequestValidationFactory.STATUS_NOT_UPDATABLE));
        return runValidations(rules, cr);
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequest cr) {
        List<String> errorMessages = new ArrayList<String>();
        ChangeRequestValidationContext context = new ChangeRequestValidationContext(cr, changeRequestDAO,
                changeRequestTypeDAO, changeRequestStatusTypeDAO, developerDAO);

        for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }

    private boolean isWebsiteChangeRequest(final ChangeRequest cr) {
        return cr.getChangeRequestType().getId().equals(websiteChangeRequestType);
    }

    private boolean isChangeRequestAccepted(final ChangeRequest cr) {
        // Assume current status is correct
        return cr.getCurrentStatus().getChangeRequestStatusType().getId().equals(acceptedStatus);
    }
}
