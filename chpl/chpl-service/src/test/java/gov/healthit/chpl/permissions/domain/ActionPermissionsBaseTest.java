package gov.healthit.chpl.permissions.domain;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import gov.healthit.chpl.TestingUsers;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;

public abstract class ActionPermissionsBaseTest extends TestingUsers {
    public static final Long ROLE_ONC_ID = 8L;
    public static final Long ROLE_ADMIN_ID = -2L;
    public static final Long ROLE_ACB_ID = 2L;

    public abstract void hasAccess_Admin() throws Exception;

    public abstract void hasAccess_Onc() throws Exception;

    public abstract void hasAccess_Acb() throws Exception;

    public abstract void hasAccess_Cms() throws Exception;

    public abstract void hasAccess_Anon() throws Exception;

    public void hasAccess_Developer() throws Exception {
        // Do nothing - just Override if necessary
    }

    public List<CertificationBody> getAllAcbForUser(Long... acbIds) {
        List<CertificationBody> dtos = new ArrayList<CertificationBody>();

        for (Long acbId : acbIds) {
            CertificationBody dto = new CertificationBody();
            dto.setId(acbId);
            dtos.add(dto);
        }

        return dtos;
    }

    public List<Developer> getAllDeveloperForUser(Long... developerIds) {
        List<Developer> devs = new ArrayList<Developer>();

        for (Long devId : developerIds) {
            Developer dev = new Developer();
            dev.setId(devId);
            devs.add(dev);
        }
        return devs;
    }

    public CertifiedProductDTO getCertifiedProduct(Long id, Long certificationBodyId) {
        CertifiedProductDTO dto = new CertifiedProductDTO();
        dto.setId(id);
        dto.setCertificationBodyId(certificationBodyId);

        return dto;
    }

    public void setupForAnonUser(ResourcePermissions resourcePermissions) {
        SecurityContextHolder.getContext().setAuthentication(null);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleCmsStaff()).thenReturn(false);
        Mockito.when(resourcePermissions.isUserRoleUserCreator()).thenReturn(false);
    }
}
