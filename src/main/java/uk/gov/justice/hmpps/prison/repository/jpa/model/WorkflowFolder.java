package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "WORKFLOW_FOLDERS")
@Getter
@Setter
@RequiredArgsConstructor
public class WorkflowFolder extends AuditableEntity {

    @Id
    @Column(name = "WORKFLOW_CODE", nullable = false, length = 12)
    private String workflowCode;

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
        WorkflowFolder that = (WorkflowFolder) other;
        return workflowCode != null && Objects.equals(workflowCode, that.workflowCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowCode);
    }
}
