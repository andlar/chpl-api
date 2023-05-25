package gov.healthit.chpl.permissions.domain.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import gov.healthit.chpl.dao.FilterDAO;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.permissions.domain.ActionPermissionsBaseTest;
import gov.healthit.chpl.permissions.domains.filter.UpdateActionPermissions;

public class DeleteActionPermissionsTest extends ActionPermissionsBaseTest {

    @Mock
    private ResourcePermissions resourcePermissions;

    @Spy
    private FilterDAO filterDAO;

    @InjectMocks
    private UpdateActionPermissions permissions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    @Test
    public void hasAccess_Admin() throws Exception {
        setupForAdminUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO dto = getFilterDTO(1L, -2L);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -2L));
        assertTrue(permissions.hasAccess(dto));

        FilterDTO dto2 = getFilterDTO(1L, -2L);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    @Override
    @Test
    public void hasAccess_Onc() throws Exception {
        setupForOncUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO dto = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, 3l));
        assertTrue(permissions.hasAccess(dto));

        FilterDTO dto2 = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    @Override
    @Test
    public void hasAccess_OncStaff() throws Exception {
        setupForOncStaffUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO dto = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, 3l));
        assertTrue(permissions.hasAccess(dto));

        FilterDTO dto2 = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    @Override
    @Test
    public void hasAccess_Acb() throws Exception {
        setupForAcbUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO dto = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, 3l));
        assertTrue(permissions.hasAccess(dto));

        FilterDTO dto2 = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    @Override
    @Test
    public void hasAccess_Cms() throws Exception {
        setupForCmsUser(resourcePermissions);

        assertFalse(permissions.hasAccess());

        FilterDTO dto = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, 3l));
        assertTrue(permissions.hasAccess(dto));

        FilterDTO dto2 = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    @Override
    @Test
    public void hasAccess_Anon() throws Exception {
        setupForAnonUser(resourcePermissions);

        FilterDTO dto2 = getFilterDTO(1L, 3l);
        Mockito.when(filterDAO.getById(ArgumentMatchers.anyLong())).thenReturn(getFilterDTO(1L, -5l));
        assertFalse(permissions.hasAccess(dto2));
    }

    private FilterDTO getFilterDTO(Long id, Long userId) {
        FilterDTO filterDTO = new FilterDTO();
        filterDTO.setId(id);
        filterDTO.setFilter("{}");
        filterDTO.setFilterType(new FilterTypeDTO());
        filterDTO.getFilterType().setId(1L);
        filterDTO.getFilterType().setName("SAMPLE_FILTER");
        filterDTO.setUser(new UserDTO());
        filterDTO.getUser().setId(userId);
        return filterDTO;
    }
}
