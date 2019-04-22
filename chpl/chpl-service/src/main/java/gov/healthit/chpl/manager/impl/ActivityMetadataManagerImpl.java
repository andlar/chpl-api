package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;

import gov.healthit.chpl.activity.ActivityMetadataBuilder;
import gov.healthit.chpl.activity.ActivityMetadataBuilderFactory;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityMetadataManager;
import gov.healthit.chpl.permissions.Permissions;

@Service("activityMetadataManager")
public class ActivityMetadataManagerImpl implements ActivityMetadataManager {
    private static final Logger LOGGER = LogManager.getLogger(ActivityMetadataManagerImpl.class);

    private ActivityDAO activityDAO;
    private ActivityMetadataBuilderFactory metadataBuilderFactory;

    @Autowired
    public ActivityMetadataManagerImpl(final ActivityDAO activityDAO,
            final ActivityMetadataBuilderFactory metadataBuilderFactory) {
        this.activityDAO = activityDAO;
        this.metadataBuilderFactory = metadataBuilderFactory;
    }

    @Transactional
    public List<ActivityMetadata> getActivityMetadataByConcept(
            final ActivityConcept concept, final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting " + concept.name() + " activity from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByConcept(concept, startDate, endDate);
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            //excpect all dtos to have the same
            //since we've searched based on activity concept
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            //convert to domain object
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        }
        return activityMetas;
    }

    @Transactional
    public List<ActivityMetadata> getActivityMetadataByObject(
            final Long objectId, final ActivityConcept concept,
            final Date startDate, final Date endDate)
            throws JsonParseException, IOException {

        LOGGER.info("Getting " + concept.name() + " activity for id " + objectId + " from " + startDate + " through " + endDate);
        //get the activity
        List<ActivityDTO> activityDtos = activityDAO.findByObjectId(objectId, concept, startDate, endDate);
        List<ActivityMetadata> activityMetas = new ArrayList<ActivityMetadata>();
        ActivityMetadataBuilder builder = null;
        if (activityDtos != null && activityDtos.size() > 0) {
            //excpect all dtos to have the same
            //since we've searched based on activity concept
            builder = metadataBuilderFactory.getBuilder(activityDtos.get(0));
            //convert to domain object
            for (ActivityDTO dto : activityDtos) {
                ActivityMetadata activityMeta = builder.build(dto);
                activityMetas.add(activityMeta);
            }
        }
        return activityMetas;
    }
}