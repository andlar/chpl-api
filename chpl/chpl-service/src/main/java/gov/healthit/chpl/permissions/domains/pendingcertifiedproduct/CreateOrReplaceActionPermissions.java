package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductCreateOrReplaceActionPermissions")
public class CreateOrReplaceActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (Util.isUserRoleAdmin() || Util.isUserRoleOnc()) {
            return true;
        } else if (Util.isUserRoleAcbAdmin()) {
            Long acbId = (Long) obj;
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }
}
