package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "OFFENDER_IEP_LEVELS")
@Entity
@ToString
@IdClass(OffenderIepLevel.PK.class)
public class OffenderIepLevel extends ExtendedAuditableEntity {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private OffenderBooking offenderBooking;
        private Long sequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @Exclude
    private OffenderBooking offenderBooking;

    @Id
    @Column(name = "IEP_LEVEL_SEQ", nullable = false)
    private Long sequence;

    @Column(name = "IEP_DATE", nullable = false)
    private LocalDate iepDate;

    @Column(name = "IEP_TIME")
    private LocalDateTime iepDateTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation agencyLocation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="IEP_LEVEL", referencedColumnName="IEP_LEVEL", updatable = false, insertable = false),
        @JoinColumn(name="AGY_LOC_ID", referencedColumnName="AGY_LOC_ID", updatable = false, insertable = false)
    })
    @Exclude
    private AvailablePrisonIepLevel availablePrisonIepLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + IepLevel.IEP_LEVEL + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "IEP_LEVEL", referencedColumnName = "code"))
    })
    @Exclude
    private IepLevel iepLevel;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "USER_ID")
    @Exclude
    private StaffUserAccount staffUser;

    @Column(name = "USER_ID", updatable = false, insertable = false)
    private String username;

    public PrivilegeDetail getIepSummary() {
        return PrivilegeDetail.builder()
            .bookingId(getOffenderBooking().getBookingId())
            .iepDate(getIepDate())
            .iepTime(getIepDateTime())
            .iepLevel(getIepLevel().getDescription())
            .agencyId(getAvailablePrisonIepLevel().getAgencyLocation().getId())
            .comments(getComment())
            .userId(getStaffUser() != null ? getStaffUser().getUsername() : getUsername())
            .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final OffenderIepLevel that = (OffenderIepLevel) o;

        if (!Objects.equals(getOffenderBooking(), that.getOffenderBooking())) return false;
        return Objects.equals(getSequence(), that.getSequence());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getOffenderBooking());
        result = 31 * result + (Objects.hashCode(getSequence()));
        return result;
    }
}