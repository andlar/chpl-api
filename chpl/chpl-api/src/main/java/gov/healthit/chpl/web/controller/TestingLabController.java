package gov.healthit.chpl.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.results.TestingLabResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "atls", description = "Allows management of testing labs (ONC-ATLs).")
@RestController
@RequestMapping("/atls")
public class TestingLabController {

    @Autowired
    private TestingLabManager atlManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private ResourcePermissions resourcePermissions;

    @Autowired
    private UserPermissionsManager userPermissionsManager;

    @Operation(summary = "List all testing labs (ATLs).",
            description = "ROLE_ADMIN and ROLE_ONC can view and edit ONC-ATLs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody TestingLabResults getAtls(
            @RequestParam(required = false, defaultValue = "false") final boolean editable) {
        TestingLabResults results = new TestingLabResults();
        atlManager.getAll().stream()
                .forEach(atlDto -> results.getAtls().add(new TestingLab(atlDto)));
        return results;
    }

    @Operation(summary = "Get details about a specific testing lab (ATL).",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{atlId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TestingLab getAtlById(@PathVariable("atlId") final Long atlId)
            throws EntityRetrievalException {
        TestingLabDTO atl = atlManager.getById(atlId);

        return new TestingLab(atl);
    }

    @Operation(summary = "Create a new testing lab.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC can create a new testing lab.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public TestingLab createAtl(@RequestBody final TestingLab atlInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException,
            EntityCreationException, JsonProcessingException {

        return create(atlInfo);
    }

    private TestingLab create(final TestingLab atlInfo) throws InvalidArgumentsException, UserRetrievalException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException {
        TestingLabDTO toCreate = new TestingLabDTO();
        toCreate.setTestingLabCode(atlInfo.getAtlCode());
        toCreate.setAccredidationNumber(atlInfo.getAccredidationNumber());
        if (StringUtils.isEmpty(atlInfo.getName())) {
            throw new InvalidArgumentsException("A name is required for a testing lab");
        }
        toCreate.setName(atlInfo.getName());
        toCreate.setWebsite(atlInfo.getWebsite());

        if (atlInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required for a new testing lab");
        }
        toCreate.setAddress(atlInfo.getAddress());
        toCreate = atlManager.create(toCreate);
        return new TestingLab(toCreate);
    }

    @Operation(summary = "Update an existing ATL.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{atlId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<TestingLab> updateAtl(@RequestBody final TestingLab atlInfo)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {

        return update(atlInfo);
    }

    private ResponseEntity<TestingLab> update(final TestingLab updatedAtl) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        TestingLabDTO existingAtl = atlManager.getById(updatedAtl.getId());
        if (updatedAtl.isRetired()) {
            // we are retiring this ATL and no other changes can be made
            existingAtl.setRetired(true);
            existingAtl.setRetirementDate(updatedAtl.getRetirementDate());
            atlManager.retire(existingAtl);
        } else {
            if (existingAtl.isRetired()) {
                // unretire the ATL
                atlManager.unretire(updatedAtl.getId());
            }
            TestingLabDTO toUpdate = new TestingLabDTO();
            toUpdate.setId(updatedAtl.getId());
            toUpdate.setTestingLabCode(updatedAtl.getAtlCode());
            toUpdate.setRetired(false);
            toUpdate.setRetirementDate(null);
            toUpdate.setAccredidationNumber(updatedAtl.getAccredidationNumber());
            if (StringUtils.isEmpty(updatedAtl.getName())) {
                throw new InvalidArgumentsException("A name is required for a testing lab");
            }
            toUpdate.setName(updatedAtl.getName());
            toUpdate.setWebsite(updatedAtl.getWebsite());

            if (updatedAtl.getAddress() == null) {
                throw new InvalidArgumentsException("An address is required to update the testing lab");
            }
            toUpdate.setAddress(updatedAtl.getAddress());
            atlManager.update(toUpdate);
        }

        TestingLabDTO result = atlManager.getById(updatedAtl.getId());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        TestingLab response = new TestingLab(result);
        return new ResponseEntity<TestingLab>(response, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Remove user permissions from an ATL.",
            description = "This endpoint is deprecated and has no effect.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApi(httpMethod = "DELETE", friendlyUrl = "atls/{atlId}/users/{userId}",
            message = "This endpoint will be removedl", removalDate = "2023-11-30")
    @RequestMapping(value = "{atlId}/users/{userId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public String deleteUserFromAtl(@PathVariable final Long atlId, @PathVariable final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException, EntityCreationException {

        return "{\"userDeleted\" : false}";
    }

    @Operation(summary = "List users with permissions on a specified ATL.",
            description = "This endpoint is deprecated and will return an empty response object.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "atls/{atlId}/users",
            message = "This endpoint will be removedl", removalDate = "2023-11-30")
    @RequestMapping(value = "/{atlId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UsersResponse getUsers(@PathVariable("atlId") final Long atlId)
            throws InvalidArgumentsException, EntityRetrievalException {
        return new UsersResponse();
    }
}
