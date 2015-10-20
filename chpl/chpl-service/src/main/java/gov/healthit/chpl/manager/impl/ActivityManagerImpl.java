package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.JSONUtils;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.ActivityEvent;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.manager.ActivityManager;















import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ActivityManagerImpl implements ActivityManager {

	@Autowired
	ActivityDAO activityDAO;
	
	private ObjectMapper jsonMapper = new ObjectMapper();
	private JsonFactory factory = jsonMapper.getFactory();
	
	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setOriginalData(JSONUtils.toJSON(originalData));
		dto.setNewData(JSONUtils.toJSON(newData));
		dto.setActivityDate(new Date());
		dto.setActivityObjectId(objectId);
		dto.setCreationDate(new Date());
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setDeleted(false);
		
		activityDAO.create(dto);
		
	}

	@Override
	@Transactional
	public void addActivity(ActivityConcept concept, Long objectId,
			String activityDescription, Object originalData, Object newData,
			Date timestamp) throws EntityCreationException,
			EntityRetrievalException, JsonProcessingException {
		
		ActivityDTO dto = new ActivityDTO();
		dto.setConcept(concept);
		dto.setId(null);
		dto.setDescription(activityDescription);
		dto.setOriginalData(JSONUtils.toJSON(originalData));
		dto.setNewData(JSONUtils.toJSON(newData));
		dto.setActivityDate(timestamp);
		dto.setActivityObjectId(objectId);
		dto.setCreationDate(new Date());
		dto.setLastModifiedDate(new Date());
		dto.setLastModifiedUser(Util.getCurrentUser().getId());
		dto.setDeleted(false);
		
		activityDAO.create(dto);
		
	}
	
	@Override
	@Transactional
	public List<ActivityEvent> getAllActivity() throws JsonParseException, IOException {
		List<ActivityDTO> dtos = activityDAO.findAll();
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(
			ActivityConcept concept, Long objectId) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(objectId, concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}


	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(concept);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}
	
	@Override
	@Transactional
	public List<ActivityEvent> getAllActivityInLastNDays(Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findAllInLastNDays(lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForObject(
			ActivityConcept concept, Long objectId, Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByObjectId(objectId, concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}

	@Override
	@Transactional
	public List<ActivityEvent> getActivityForConcept(ActivityConcept concept, Integer lastNDays) throws JsonParseException, IOException {
		
		List<ActivityDTO> dtos = activityDAO.findByConcept(concept, lastNDays);
		List<ActivityEvent> events = new ArrayList<ActivityEvent>();
		
		for (ActivityDTO dto : dtos){
			ActivityEvent event = getActivityEventFromDTO(dto);
			events.add(event);
		}
		return events;
	}
	
	@Override
	@Transactional
	public void deleteActivity(Long toDelete) throws EntityRetrievalException{
		
		ActivityDTO dto = activityDAO.getById(toDelete);
		dto.setDeleted(true);
		activityDAO.update(dto);
		
	}
	

	private ActivityEvent getActivityEventFromDTO(ActivityDTO dto) throws JsonParseException, IOException{
		
		ActivityEvent event = new ActivityEvent();
		
		event.setId(dto.getId());
		event.setDescription(dto.getDescription());
		event.setActivityDate(dto.getActivityDate());
		event.setActivityObjectId(dto.getActivityObjectId());
		event.setConcept(dto.getConcept());			
		
		JsonNode originalJSON = null;
		if (dto.getOriginalData()!= null){
			JsonParser origData = factory.createParser(dto.getOriginalData());
			originalJSON = jsonMapper.readTree(origData);
		}
		
		JsonNode newJSON = null;
		if (dto.getNewData()!= null){
			JsonParser newData = factory.createParser(dto.getNewData());
			newJSON = jsonMapper.readTree(newData);
		}
		
		event.setOriginalData(originalJSON);
		event.setNewData(newJSON);
		
		return event;
	}

}