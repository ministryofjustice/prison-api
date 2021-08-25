package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
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
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDER_IMPRISON_STATUSES")
@Builder(toBuilder = true)
@AllArgsConstructor
@IdClass(OffenderImprisonmentStatus.PK.class)
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
    @Exclude
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "IMPRISON_STATUS_SEQ", nullable = false)
    private Long imprisonStatusSeq;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "IMPRISONMENT_STATUS", nullable = false, referencedColumnName = "IMPRISONMENT_STATUS")
    @Exclude
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderImprisonmentStatus that = (OffenderImprisonmentStatus) o;

        if (!Objects.equals(offenderBooking, that.offenderBooking)) return false;
        return Objects.equals(imprisonStatusSeq, that.imprisonStatusSeq);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(offenderBooking);
        result = 31 * result + (Objects.hashCode(imprisonStatusSeq));
        return result;
    }
}
