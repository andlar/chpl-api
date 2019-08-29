package gov.healthit.chpl.dao.changerequest;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestConverter;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatus;
import gov.healthit.chpl.entity.changerequest.ChangeRequestEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestStatusEntity;
import gov.healthit.chpl.entity.changerequest.ChangeRequestTypeEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("changeRequestDAO")
public class ChangeRequestDAOImpl extends BaseDAOImpl implements ChangeRequestDAO {

    @Override
    public ChangeRequest create(final ChangeRequest cr) throws EntityRetrievalException {
        ChangeRequestEntity entity = getNewEntity(cr);
        create(entity);
        return ChangeRequestConverter.convert(getEntityById(entity.getId()));
    }

    @Override
    public ChangeRequest get(final Long changeRequestId) throws EntityRetrievalException {
        ChangeRequest cr = ChangeRequestConverter.convert(getEntityById(changeRequestId));
        cr.setCurrentStatus(getCurrentStatus(cr.getId()));
        return cr;
    }

    private ChangeRequestEntity getEntityById(final Long id) throws EntityRetrievalException {
        String hql = "SELECT DISTINCT cr "
                + "FROM ChangeRequestEntity cr "
                + "JOIN FETCH cr.changeRequestType "
                + "JOIN FETCH cr.developer "
                + "WHERE cr.deleted = false "
                + "AND cr.id = :changeRequestId";

        ChangeRequestEntity entity = null;
        List<ChangeRequestEntity> result = entityManager
                .createQuery(hql, ChangeRequestEntity.class)
                .setParameter("changeRequestId", id)
                .getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate change request id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private ChangeRequestStatus getCurrentStatus(final Long changeRequestId) {
        String hql = "SELECT crStatus "
                + "FROM ChangeRequestStatusEntity crStatus "
                + "WHERE crStatus.deleted = false "
                + "AND crStatus.changeRequest.id = :changeRequestId "
                + "ORDER BY crStatus.statusChangeDate DESC";

        List<ChangeRequestStatus> statuses = entityManager
                .createQuery(hql, ChangeRequestStatusEntity.class)
                .setParameter("changeRequestId", changeRequestId)
                .getResultList().stream()
                .map(ChangeRequestConverter::convert)
                .collect(Collectors.<ChangeRequestStatus> toList());

        if (statuses.size() > 0) {
            return statuses.get(0);
        } else {
            return null;
        }
    }

    private ChangeRequestEntity getNewEntity(ChangeRequest cr) {
        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setChangeRequestType(
                getSession().load(ChangeRequestTypeEntity.class, cr.getChangeRequestType().getId()));
        entity.setDeveloper(getSession().load(DeveloperEntity.class, cr.getDeveloper().getDeveloperId()));
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        return entity;
    }
}
