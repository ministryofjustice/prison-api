package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "COPY_TABLES")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@IdClass(CopyTable.PK.class)
@EqualsAndHashCode(callSuper = false)
public class CopyTable extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "TABLE_OPERATION_CODE", updatable = false, insertable = false, nullable = false)
        private String operationCode;
        @Column(name = "MOVEMENT_TYPE", updatable = false, insertable = false, nullable = false)
        private String movementType;
        @Column(name = "MOVEMENT_REASON_CODE", updatable = false, insertable = false, nullable = false)
        private String movementReasonCode;
        @Column(name = "TABLE_NAME", updatable = false, insertable = false, nullable = false)
        private String tableName;
    }

    @Id
    private String operationCode;
    @Id
    private String movementType;
    @Id
    private String movementReasonCode;
    @Id
    private String tableName;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @Column(name = "LIST_SEQ", nullable = false)
    private Integer listSequence;

    @Column(name = "COL_NAME")
    private String colName;

    @Column(name = "SEQ_NAME")
    private String seqName;

    @Column(name = "PARENT_TABLE")
    private String parentTable;

    @Column(name = "UPDATE_ALLOWED_FLAG", nullable = false)
    private String updateAllowedFlag;

}
