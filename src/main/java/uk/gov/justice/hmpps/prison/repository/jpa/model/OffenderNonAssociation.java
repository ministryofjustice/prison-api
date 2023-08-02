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

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Optional;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "OFFENDER_NON_ASSOCIATIONS")
@ToString(exclude = {"offender", "nsOffender", "offenderBooking"})
@IdClass(OffenderNonAssociation.Pk.class)
public class OffenderNonAssociation extends AuditableEntity {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Offender offender;

        private Offender nsOffender;
    }

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_ID", nullable = false, insertable = false, updatable = false)
    private Offender offender;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "NS_OFFENDER_ID", nullable = false, insertable = false, updatable = false, referencedColumnName = "OFFENDER_ID")
    private Offender nsOffender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "NS_OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking nsOffenderBooking;

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
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + NonAssociationReason.DOMAIN + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "RECIP_NS_REASON_CODE", referencedColumnName = "code"))
    })
    private NonAssociationReason recipNonAssociationReason;

    public Optional<String> getNsAgencyDescription() {
        return nsOffender.getLatestBooking().map(OffenderBooking::getLocation).map(AgencyLocation::getDescription);
    }

    public Optional<String> getNsAgencyId() {
        return nsOffender.getLatestBooking().map(OffenderBooking::getLocation).map(AgencyLocation::getId);
    }

    public Optional<String> getNsAssignedLivingUnitDescription() {
        return nsOffender.getLatestBooking().map(OffenderBooking::getAssignedLivingUnit).map(AgencyInternalLocation::getDescription);
    }

    public Optional<Long> getNsAssignedLivingUnitId() {
        return nsOffender.getLatestBooking().map(OffenderBooking::getAssignedLivingUnit).map(AgencyInternalLocation::getLocationId);
    }
}
