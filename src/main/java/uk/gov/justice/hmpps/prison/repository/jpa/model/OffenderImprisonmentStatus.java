package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "OFFENDER_IMPRISON_STATUSES")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(OffenderImprisonmentStatus.PK.class)
@EqualsAndHashCode(callSuper = false)
// TODO get annotations correct (nullability)
public class OffenderImprisonmentStatus extends AuditableEntity {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Id
        @Column(name = "OFFENDER_BOOK_ID", updatable = false, insertable = false)
        private Long offenderBookId;
        @Id
        @Column(name = "IMPRISON_STATUS_SEQ", updatable = false, insertable = false)
        private Long imprisonStatusSeq;
    }


    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long offenderBookId;

    @Id
    @Column(name = "IMPRISON_STATUS_SEQ")
    private Long imprisonStatusSeq;

    @Column(name = "IMPRISONMENT_STATUS")
    private String imprisonmentStatus;

    @Column(name = "EFFECTIVE_DATE")
    private LocalDate effectiveDate;

    @Column(name = "EFFECTIVE_TIME")
    private LocalDateTime effectiveTime;

    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "AGY_LOC_ID")
    private String agyLocId;

    @Column(name = "COMMENT_TEXT")
    private String commentText;

    @Column(name = "LATEST_STATUS")
    private String latestStatus;

    @Column(name = "CREATE_DATE")
    @CreatedDate
    private LocalDate createDate;

}
