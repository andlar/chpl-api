package gov.healthit.chpl.questionableactivity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityBase;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityDeveloper;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityProduct;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityVersion;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityBaseEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityCertificationResultEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityDeveloperEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityListingEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityProductEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityTriggerEntity;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityVersionEntity;
import gov.healthit.chpl.util.ChplUserToCognitoUserUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("questionableActivityDao")
public class QuestionableActivityDAO extends BaseDAOImpl {
    private ActivityDAO activityDAO;
    private ChplUserToCognitoUserUtil chplUserToCognitoUserUtil;

    @Autowired
    public QuestionableActivityDAO(ChplUserToCognitoUserUtil chplUserToCognitoUserUtil, ActivityDAO activityDAO) {
        this.chplUserToCognitoUserUtil = chplUserToCognitoUserUtil;
        this.activityDAO = activityDAO;
    }

    @Transactional
    public QuestionableActivityBase create(QuestionableActivityBase qa) {
        QuestionableActivityBase created = null;
        QuestionableActivityBaseEntity toCreate = null;
        if (qa instanceof QuestionableActivityVersion) {
            toCreate = new QuestionableActivityVersionEntity();
            QuestionableActivityVersionEntity versionActivity = (QuestionableActivityVersionEntity) toCreate;
            versionActivity.setVersionId(((QuestionableActivityVersion) qa).getVersionId());
        } else if (qa instanceof QuestionableActivityProduct) {
            toCreate = new QuestionableActivityProductEntity();
            QuestionableActivityProductEntity productActivity = (QuestionableActivityProductEntity) toCreate;
            productActivity.setProductId(((QuestionableActivityProduct) qa).getProductId());
        } else if (qa instanceof QuestionableActivityDeveloper) {
            toCreate = new QuestionableActivityDeveloperEntity();
            QuestionableActivityDeveloperEntity developerActivity = (QuestionableActivityDeveloperEntity) toCreate;
            developerActivity.setDeveloperId(((QuestionableActivityDeveloper) qa).getDeveloperId());
            developerActivity.setReason(((QuestionableActivityDeveloper) qa).getReason());
        } else if (qa instanceof QuestionableActivityListing) {
            toCreate = new QuestionableActivityListingEntity();
            QuestionableActivityListingEntity listingActivity = (QuestionableActivityListingEntity) toCreate;
            listingActivity.setListingId(((QuestionableActivityListing) qa).getListingId());
            listingActivity.setCertificationStatusChangeReason(
                    ((QuestionableActivityListing) qa).getCertificationStatusChangeReason());
            listingActivity.setReason(((QuestionableActivityListing) qa).getReason());
        } else if (qa instanceof QuestionableActivityCertificationResult) {
            toCreate = new QuestionableActivityCertificationResultEntity();
            QuestionableActivityCertificationResultEntity certResultActivity = (QuestionableActivityCertificationResultEntity) toCreate;
            certResultActivity.setCertResultId(((QuestionableActivityCertificationResult) qa).getCertResultId());
            certResultActivity.setReason(((QuestionableActivityCertificationResult) qa).getReason());
        } else {
            LOGGER.error("Unknown class of questionable activity passed in: " + qa.getClass().getName());
            return null;
        }

        toCreate.setActivity(getActivityEntity(qa.getActivity().getId()));

        toCreate.setActivityDate(qa.getActivityDate());
        toCreate.setBefore(qa.getBefore());
        toCreate.setAfter(qa.getAfter());
        toCreate.setTriggerId(qa.getTrigger().getId());
        entityManager.persist(toCreate);
        entityManager.flush();
        entityManager.clear();

        if (toCreate instanceof QuestionableActivityVersionEntity) {
            created = mapEntityToDomain((QuestionableActivityVersionEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityProductEntity) {
            created = mapEntityToDomain((QuestionableActivityProductEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityDeveloperEntity) {
            created = mapEntityToDomain((QuestionableActivityDeveloperEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityListingEntity) {
            created = mapEntityToDomain((QuestionableActivityListingEntity) toCreate);
        } else if (toCreate instanceof QuestionableActivityCertificationResultEntity) {
            created = mapEntityToDomain((QuestionableActivityCertificationResultEntity) toCreate);
        }
        return created;
    }

    @Transactional
    public List<QuestionableActivityTrigger> getAllTriggers() {
        Query query = entityManager.createQuery("SELECT trigger "
                + "FROM QuestionableActivityTriggerEntity trigger "
                + "WHERE trigger.deleted <> true",
                QuestionableActivityTriggerEntity.class);
        List<QuestionableActivityTriggerEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityVersion> findVersionActivityBetweenDates(Date start, Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityVersionEntity activity "
                + "LEFT OUTER JOIN FETCH activity.version "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.activity "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityVersionEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityVersionEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> mapEntityToDomain(entity))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityProduct> findProductActivityBetweenDates(Date start, Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityProductEntity activity "
                + "LEFT OUTER JOIN FETCH activity.product "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.activity "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityProductEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityProductEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> mapEntityToDomain(entity))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityDeveloper> findDeveloperActivityBetweenDates(Date start, Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityDeveloperEntity activity "
                + "LEFT OUTER JOIN FETCH activity.developer "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.activity "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityDeveloperEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityDeveloperEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> mapEntityToDomain(entity))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityListing> findListingActivityBetweenDates(Date start, Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityListingEntity activity "
                + "LEFT OUTER JOIN FETCH activity.listing "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.activity "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityListingEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityListingEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> mapEntityToDomain(entity))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<QuestionableActivityCertificationResult> findCertificationResultActivityBetweenDates(
            Date start, Date end) {
        Query query = entityManager.createQuery("SELECT activity "
                + "FROM QuestionableActivityCertificationResultEntity activity "
                + "LEFT OUTER JOIN FETCH activity.certResult certResult "
                + "LEFT OUTER JOIN FETCH certResult.listing "
                + "LEFT OUTER JOIN FETCH activity.trigger "
                + "LEFT OUTER JOIN FETCH activity.activity "
                + "WHERE activity.deleted <> true "
                + "AND activity.activityDate >= :startDate "
                + "AND activity.activityDate <= :endDate",
                QuestionableActivityCertificationResultEntity.class);
        query.setParameter("startDate", start);
        query.setParameter("endDate", end);
        List<QuestionableActivityCertificationResultEntity> queryResults = query.getResultList();
        return queryResults.stream()
                .map(entity -> mapEntityToDomain(entity))
                .collect(Collectors.toList());
    }

    private QuestionableActivityVersion mapEntityToDomain(QuestionableActivityVersionEntity entity) {
        QuestionableActivityVersion qa = entity.toDomain();
        qa.setUser(getUserFromActivtyEntity(entity.getActivity()));
        return qa;
    }

    private QuestionableActivityProduct mapEntityToDomain(QuestionableActivityProductEntity entity) {
        QuestionableActivityProduct qa = entity.toDomain();
        qa.setUser(getUserFromActivtyEntity(entity.getActivity()));
        return qa;
    }

    private QuestionableActivityDeveloper mapEntityToDomain(QuestionableActivityDeveloperEntity entity) {
        QuestionableActivityDeveloper qa = entity.toDomain();
        qa.setUser(getUserFromActivtyEntity(entity.getActivity()));
        return qa;
    }

    private QuestionableActivityListing mapEntityToDomain(QuestionableActivityListingEntity entity) {
        QuestionableActivityListing qa = entity.toDomain();
        qa.setUser(getUserFromActivtyEntity(entity.getActivity()));
        return qa;
    }

    private QuestionableActivityCertificationResult mapEntityToDomain(QuestionableActivityCertificationResultEntity entity) {
        QuestionableActivityCertificationResult qa = entity.toDomain();
        qa.setUser(getUserFromActivtyEntity(entity.getActivity()));
        return qa;
    }

    private User getUserFromActivtyEntity(ActivityEntity entity) {
        return chplUserToCognitoUserUtil.getUser(entity.getLastModifiedUser(), entity.getLastModifiedSsoUser());
    }

    private ActivityEntity getActivityEntity(Long activityId) {
        try {
            return activityDAO.getEntityById(activityId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve activity with Id: {}", activityId, e);
            return null;
        }
    }
}
