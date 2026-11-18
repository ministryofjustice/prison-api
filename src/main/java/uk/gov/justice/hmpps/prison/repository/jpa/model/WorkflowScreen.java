package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "WORKFLOW_SCREENS")
@Getter
@Setter
@RequiredArgsConstructor
public class WorkflowScreen extends AuditableEntity {

    @EmbeddedId
    private PK id;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "WORKFLOW_CODE", nullable = false, length = 12)
        private String workflowCode;

        @Column(name = "MODULE_NAME", nullable = false, length = 20)
        private String moduleName;
    }

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "WORKFLOW_SEQ")
    private Integer workflowSeq;

    @Column(name = "CASELOAD_TYPE", length = 12)
    private String caseloadType;

    @Column(name = "AUDIT_TIMESTAMP")
    private LocalDateTime auditTimestamp;

    @Column(name = "AUDIT_USER_ID", length = 32)
    private String auditUserId;

    @Column(name = "AUDIT_MODULE_NAME", length = 65)
    private String auditModuleName;

    @Column(name = "AUDIT_CLIENT_USER_ID", length = 64)
    private String auditClientUserId;

    @Column(name = "AUDIT_CLIENT_IP_ADDRESS", length = 39)
    private String auditClientIpAddress;

    @Column(name = "AUDIT_CLIENT_WORKSTATION_NAME", length = 64)
    private String auditClientWorkstationName;

    @Column(name = "AUDIT_ADDITIONAL_INFO", length = 256)
    private String auditAdditionalInfo;

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false;
        WorkflowScreen that = (WorkflowScreen) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
