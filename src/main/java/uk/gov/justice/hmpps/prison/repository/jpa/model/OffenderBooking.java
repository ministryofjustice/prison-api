package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ListIndexBase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
public class OffenderBooking {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @OrderColumn(name = "MILITARY_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "bookingAndSequence.offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderMilitaryRecord> militaryRecords;

    @OrderColumn(name = "CASE_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderCourtCase> courtCases;

    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderPropertyContainer> propertyContainers;

    @ManyToOne(optional = false)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation location;

    @Setter(AccessLevel.NONE)
    @Column(name = "BOOKING_SEQ", nullable = false)
    private Integer bookingSequence;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private Offender offender;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIVING_UNIT_ID")
    private AgencyInternalLocation assignedLivingUnit;

    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;

    public void add(final OffenderMilitaryRecord omr) {
        militaryRecords.add(omr);
        omr.setBookingAndSequence(new BookingAndSequence(this, militaryRecords.size()));
    }

    public void add(final OffenderCourtCase courtCase) {
        courtCases.add(courtCase);
        courtCase.setOffenderBooking(this);
    }

    public Optional<OffenderCourtCase> getCourtCaseBy(final Long courtCaseId) {
        return courtCases == null ? Optional.empty() : courtCases.stream().filter(cc -> cc.getId().equals(courtCaseId)).findFirst();
    }

    public boolean isActive() {
        return activeFlag != null && activeFlag.equals("Y");
    }

    public List<OffenderCourtCase> getActiveCourtCases() {
        return courtCases.stream().filter(OffenderCourtCase::isActive).collect(toUnmodifiableList());
    }

    public List<OffenderPropertyContainer> getActivePropertyContainers() {
        return propertyContainers.stream().filter(OffenderPropertyContainer::isActive).collect(toUnmodifiableList());
    }
}
