package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = {"offender", "nsOffender", "offenderBooking", "nonAssociation"})
@Table(name = "OFFENDER_NA_DETAILS")
@IdClass(OffenderNonAssociationDetail.Pk.class)
public class OffenderNonAssociationDetail extends AuditableEntity {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Offender offender;

        private Offender nsOffender;

        private Integer typeSequence;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_ID", nullable = false, insertable = false, updatable = false)
    private Offender offender;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "NS_OFFENDER_ID", nullable = false, insertable = false, updatable = false, referencedColumnName = "OFFENDER_ID")
    private Offender nsOffender;

    @Id
    @Column(name = "TYPE_SEQ", nullable = false, insertable = false, updatable = false)
    private Integer typeSequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + NonAssociationReason.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "NS_REASON_CODE", referencedColumnName = "code"))
    })
    private NonAssociationReason nonAssociationReason;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + NonAssociationType.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "NS_TYPE", referencedColumnName = "code"))
    })
    private NonAssociationType nonAssociationType;

    @Column(name = "NS_EFFECTIVE_DATE", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "NS_EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "AUTHORIZED_STAFF")
    private String authorizedBy;

    @Column(name = "COMMENT_TEXT")
    private String comments;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + NonAssociationReason.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RECIP_NS_REASON_CODE", referencedColumnName = "code"))
    })
    private NonAssociationReason recipNonAssociationReason;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column = @JoinColumn(name = "OFFENDER_ID", referencedColumnName = "OFFENDER_ID")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "NS_OFFENDER_ID", referencedColumnName = "NS_OFFENDER_ID")),
    })
    private OffenderNonAssociation nonAssociation;

    public Optional<String> getAgencyDescription() {
        return Optional.ofNullable(offenderBooking.getLocation()).map(AgencyLocation::getDescription);
    }

    public Optional<String> getAssignedLivingUnitDescription() {
        return Optional.ofNullable(offenderBooking.getAssignedLivingUnit()).map(AgencyInternalLocation::getDescription);
    }
}

