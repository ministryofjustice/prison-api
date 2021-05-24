package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "OFFENDER_IMPRISON_STATUSES")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@IdClass(OffenderImprisonmentStatus.PK.class)
@EqualsAndHashCode(of = {"offenderBooking", "imprisonStatusSeq"}, callSuper = false)
@ToString(of = {"imprisonmentStatus", "latestStatus", "imprisonStatusSeq", "effectiveDate", "expiryDate"})
public class OffenderImprisonmentStatus extends AuditableEntity {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Long imprisonStatusSeq;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "IMPRISON_STATUS_SEQ", nullable = false)
    private Long imprisonStatusSeq;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IMPRISONMENT_STATUS", nullable = false, referencedColumnName = "IMPRISONMENT_STATUS")
    private ImprisonmentStatus imprisonmentStatus;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
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

    public boolean isActiveLatestStatus() {
        return "Y".equalsIgnoreCase(latestStatus);
    }

    public void makeActive() {
        setLatestStatus("Y");
        setExpiryDate(null);
    }

    public void makeInactive(final LocalDateTime expiryDate) {
        setExpiryDate(expiryDate);
        setLatestStatus("N");
    }

}
