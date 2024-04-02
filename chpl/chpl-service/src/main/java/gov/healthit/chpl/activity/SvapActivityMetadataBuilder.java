package gov.healthit.chpl.activity;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.activity.ActivityCategory;
import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("svapActivityMetadataBuilder")
public class SvapActivityMetadataBuilder extends ActivityMetadataBuilder {
    @Override
    protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {
        ObjectMapper jsonMapper = new ObjectMapper();
        Svap svap = null;

        if (metadata.getCategories().contains(ActivityCategory.CREATE)) {
            try {
                svap = jsonMapper.readValue(dto.getNewData(), Svap.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Svap. "
                        + "JSON was: " + dto.getNewData());
            }
        } else if (metadata.getCategories().contains(ActivityCategory.DELETE)
                || metadata.getCategories().contains(ActivityCategory.UPDATE)) {
            try {
                svap = jsonMapper.readValue(dto.getOriginalData(), Svap.class);
            } catch (Exception e) {
                LOGGER.warn("Could not parse activity ID " + dto.getId() + " new data " + "as Svap. "
                        + "JSON was: " + dto.getOriginalData());
            }
        }

        if (svap != null) {
            metadata.getObject().setName(svap.getRegulatoryTextCitation());
        }
    }
}
