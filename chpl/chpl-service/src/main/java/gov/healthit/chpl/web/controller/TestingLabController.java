package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ChplPermission;
import gov.healthit.chpl.domain.PermittedUser;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.UpdateUserAndAtlRequest;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.web.controller.results.PermittedUserResults;
import gov.healthit.chpl.web.controller.results.TestingLabResults;
import io.swagger.annotations.Api;

@Api(value="atls")
@RestController
@RequestMapping("/atls")
public class TestingLabController {
	
	@Autowired TestingLabManager atlManager;
	@Autowired UserManager userManager;
	
	private static final Logger logger = LogManager.getLogger(TestingLabController.class);
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody TestingLabResults getAtls(@RequestParam(required=false, defaultValue="false") boolean editable) {
		TestingLabResults results = new TestingLabResults();
		List<TestingLabDTO> atls = null;
		if(editable) {
			atls = atlManager.getAllForUser();
		} else {
			atls = atlManager.getAll();
		}
		
		if(atls != null) {
			for(TestingLabDTO atl : atls) {
				results.getAtls().add(new TestingLab(atl));
			}
		}
		return results;
	}
	
	@RequestMapping(value="/{atlId}", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody TestingLab getAtlById(@PathVariable("atlId") Long atlId)
		throws EntityRetrievalException {
		TestingLabDTO atl = atlManager.getById(atlId);
		
		return new TestingLab(atl);
	}
	
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public TestingLab create(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, EntityCreationException, JsonProcessingException {
		TestingLabDTO toCreate = new TestingLabDTO();
		toCreate.setTestingLabCode(atlInfo.getAtlCode());
		toCreate.setAccredidationNumber(atlInfo.getAccredidationNumber());
		if(StringUtils.isEmpty(atlInfo.getName())) {
			throw new InvalidArgumentsException("A name is required for a testing lab");
		}
		toCreate.setName(atlInfo.getName());
		toCreate.setWebsite(atlInfo.getWebsite());
		
		if(atlInfo.getAddress() == null) {
			throw new InvalidArgumentsException("An address is required for a new testing lab");
		}
		AddressDTO address = new AddressDTO();
		address.setId(atlInfo.getAddress().getAddressId());
		address.setStreetLineOne(atlInfo.getAddress().getLine1());
		address.setStreetLineTwo(atlInfo.getAddress().getLine2());
		address.setCity(atlInfo.getAddress().getCity());
		address.setState(atlInfo.getAddress().getState());
		address.setZipcode(atlInfo.getAddress().getZipcode());
		address.setCountry(atlInfo.getAddress().getCountry());
		toCreate.setAddress(address);
		toCreate = atlManager.create(toCreate);
		return new TestingLab(toCreate);
	}
	

	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public TestingLab update(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
		TestingLabDTO toUpdate = new TestingLabDTO();
		toUpdate.setId(atlInfo.getId());
		toUpdate.setTestingLabCode(atlInfo.getAtlCode());
		toUpdate.setAccredidationNumber(atlInfo.getAccredidationNumber());
		if(StringUtils.isEmpty(atlInfo.getName())) {
			throw new InvalidArgumentsException("A name is required for a testing lab");
		}
		toUpdate.setName(atlInfo.getName());
		toUpdate.setWebsite(atlInfo.getWebsite());
		
		if(atlInfo.getAddress() == null) {
			throw new InvalidArgumentsException("An address is required to update the testing lab");
		}
		AddressDTO address = new AddressDTO();
		address.setId(atlInfo.getAddress().getAddressId());
		address.setStreetLineOne(atlInfo.getAddress().getLine1());
		address.setStreetLineTwo(atlInfo.getAddress().getLine2());
		address.setCity(atlInfo.getAddress().getCity());
		address.setState(atlInfo.getAddress().getState());
		address.setZipcode(atlInfo.getAddress().getZipcode());
		address.setCountry(atlInfo.getAddress().getCountry());
		toUpdate.setAddress(address);
		
		TestingLabDTO result = atlManager.update(toUpdate);
		return new TestingLab(result);
	}
	
	
	@RequestMapping(value="/{atlId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteAtl(@PathVariable("atlId") Long atlId) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
		
		TestingLabDTO toDelete = atlManager.getById(atlId);		
		atlManager.delete(toDelete);
		return "{\"deletedAtl\" : true }";
	
	}
	
	@RequestMapping(value="/add_user", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String addUserToAtl(@RequestBody UpdateUserAndAtlRequest updateRequest) 
									throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {
		
		if(updateRequest.getAtlId() == null || updateRequest.getUserId() == null || updateRequest.getUserId() <= 0 || updateRequest.getAuthority() == null) {
			throw new InvalidArgumentsException("ATL ID, User ID (greater than 0), and Authority are required.");
		}
		
		UserDTO user = userManager.getById(updateRequest.getUserId());
		TestingLabDTO atl = atlManager.getById(updateRequest.getAtlId());
		
		if(user == null || atl == null) {
			throw new InvalidArgumentsException("Could not find either ATL or User specified");
		}

		Permission permission = ChplPermission.toPermission(updateRequest.getAuthority());
		atlManager.addPermission(atl, updateRequest.getUserId(), permission);
		return "{\"userAdded\" : true }";
	}
	
	@RequestMapping(value="{atlId}/remove_user/{userId}", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String deleteUserFromAtl(@PathVariable Long atlId, @PathVariable Long userId) 
								throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException{
		
		UserDTO user = userManager.getById(userId);
		TestingLabDTO atl = atlManager.getById(atlId);
		
		if(user == null || atl == null) {
			throw new InvalidArgumentsException("Could not find either ATL or User specified");
		}
		
		//delete all permissions on that atl
		atlManager.deleteAllPermissionsOnAtl(atl, new PrincipalSid(user.getSubjectName()));
		
		return "{\"userDeleted\" : true }";
	}
	
	@RequestMapping(value="/{atlId}/users", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody PermittedUserResults getUsers(@PathVariable("atlId") Long atlId) throws InvalidArgumentsException, EntityRetrievalException {
		TestingLabDTO atl = atlManager.getById(atlId);
		if(atl == null) {
			throw new InvalidArgumentsException("Could not find the ATL specified.");
		}
		
		List<PermittedUser> atlUsers = new ArrayList<PermittedUser>();
		List<UserDTO> users = atlManager.getAllUsersOnAtl(atl);
		for(UserDTO user : users) {
			
			//only show users that have ROLE_ATL_*
			Set<UserPermissionDTO> systemPermissions = userManager.getGrantedPermissionsForUser(user);
			boolean hasAtlPermission = false;
			for(UserPermissionDTO systemPermission : systemPermissions) {
				if(systemPermission.getAuthority().startsWith("ROLE_ATL_")) {
					hasAtlPermission = true;
				}
			}
			
			if(hasAtlPermission) {
				List<String> roleNames = new ArrayList<String>();
				for(UserPermissionDTO role : systemPermissions) {
					roleNames.add(role.getAuthority());
				}
				
				List<Permission> permissions = atlManager.getPermissionsForUser(atl, new PrincipalSid(user.getSubjectName()));
				List<String> atlPerm = new ArrayList<String>(permissions.size());
				for(Permission permission : permissions) {
					ChplPermission perm = ChplPermission.fromPermission(permission);
					if(perm != null) {
						atlPerm.add(perm.toString());
					}
				}
				
				PermittedUser userInfo = new PermittedUser();
				userInfo.setUser(new User(user));
				userInfo.setPermissions(atlPerm);
				userInfo.setRoles(roleNames);
				atlUsers.add(userInfo);
			}
		}
		
		PermittedUserResults results = new PermittedUserResults();
		results.setUsers(atlUsers);
		return results;
	}
}
