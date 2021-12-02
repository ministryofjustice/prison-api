package uk.gov.justice.hmpps.prison.repository.jpa.model;

public class AudtableEntityUtils {
    public static void setCreatedByUserId(final AuditableEntity auditableEntity, final String userId) {
        auditableEntity.setCreateUserId(userId);
    }
}
