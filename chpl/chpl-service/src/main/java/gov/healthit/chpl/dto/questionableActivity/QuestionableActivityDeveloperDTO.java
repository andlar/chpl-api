package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityDeveloperEntity;

public class QuestionableActivityDeveloperDTO extends QuestionableActivityDTO {
    private Long developerId;
    private DeveloperDTO developer;
    
    public QuestionableActivityDeveloperDTO() {
        super();
    }
    
    public QuestionableActivityDeveloperDTO(QuestionableActivityDeveloperEntity entity) {
        super(entity);
        this.developerId = entity.getDeveloperId();
        if(entity.getDeveloper() != null) {
            this.developer = new DeveloperDTO(entity.getDeveloper());
        }
    }
    
    public Class<?> getActivityObjectClass() {
        return DeveloperDTO.class;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(DeveloperDTO developer) {
        this.developer = developer;
    }
}
