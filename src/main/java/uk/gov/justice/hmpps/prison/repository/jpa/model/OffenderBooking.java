package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ListIndexBase;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail.PK;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@EqualsAndHashCode(of = "bookingId", callSuper = false)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_BOOKINGS")
@ToString(of = {"bookingId", "bookNumber", "bookingSequence", "activeFlag", "inOutStatus"})
public class OffenderBooking extends ExtendedAuditableEntity {

    @SequenceGenerator(name = "OFFENDER_BOOK_ID", sequenceName = "OFFENDER_BOOK_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_BOOK_ID")
    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Column(name = "BOOKING_NO")
    private String bookNumber;

    @Column(name = "BOOKING_TYPE")
    private String bookingType;

    @OneToMany(mappedBy = "id.offenderBooking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OffenderProfileDetail> profileDetails = new ArrayList<>();

    @OrderColumn(name = "MILITARY_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "bookingAndSequence.offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderMilitaryRecord> militaryRecords;

    @OrderColumn(name = "CASE_SEQ")
    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OffenderCourtCase> courtCases = new ArrayList<>();

    @ListIndexBase(1)
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    private List<OffenderPropertyContainer> propertyContainers;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CREATE_AGY_LOC_ID")
    private AgencyLocation createLocation;

    @Setter(AccessLevel.NONE)
    @Column(name = "BOOKING_SEQ", nullable = false)
    private Integer bookingSequence;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_ID", nullable = false)
    private Offender offender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LIVING_UNIT_ID")
    private AgencyInternalLocation assignedLivingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_STAFF_ID")
    private Staff assignedStaff;

    @Column(name = "AGENCY_IML_ID")
    private Long livingUnitMv;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Default
    private String activeFlag = "N";

    @OrderBy("effectiveDate ASC")
    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<OffenderNonAssociationDetail> nonAssociationDetails = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<ExternalMovement> externalMovements = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<OffenderImprisonmentStatus> imprisonmentStatuses = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<OffenderCaseNote> caseNotes = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<OffenderCharge> charges = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<SentenceCalculation> sentenceCalculations = new ArrayList<>();

    @OneToMany(mappedBy = "offenderBooking", cascade = CascadeType.ALL)
    @Default
    private List<SentenceTerm> terms = new ArrayList<>();

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @Column(name = "BOOKING_STATUS")
    private String bookingStatus;

    @Column(name = "STATUS_REASON")
    private String statusReason;

    @Column(name = "DISCLOSURE_FLAG", nullable = false)
    @Default
    private String disclosureFlag = "Y";

    @Column(name = "COMMUNITY_ACTIVE_FLAG", nullable = false)
    @Default
    private String communityActiveFlag = "N";

    @Column(name = "SERVICE_FEE_FLAG", nullable = false)
    @Default
    private String serviceFeeFlag = "N";

    @Column(name = "COMM_STATUS")
    private String commStatus;

    @Column(name = "YOUTH_ADULT_CODE", nullable = false)
    private String youthAdultCode;

    @Column(name = "BOOKING_BEGIN_DATE", nullable = false)
    private LocalDateTime bookingBeginDate;

    @Column(name = "BOOKING_END_DATE")
    private LocalDateTime bookingEndDate;

    @Column(name = "IN_OUT_STATUS", nullable = false)
    private String inOutStatus;

    @Column(name = "ADMISSION_REASON")
    private String admissionReason;

    public void add(final OffenderMilitaryRecord omr) {
        militaryRecords.add(omr);
        omr.setBookingAndSequence(new BookingAndSequence(this, militaryRecords.size()));
    }

    public void add(final OffenderCourtCase courtCase) {
        courtCases.add(courtCase);
        courtCase.setOffenderBooking(this);
    }

    public void add(final ProfileType profileType, final ProfileCode code) {
        profileDetails.stream()
            .filter(pd -> profileType.equals(pd.getId().getType()))
            .max(Comparator.comparing(op -> op.getId().getSequence()))
            .ifPresentOrElse(
                y -> y.setCode(code)
                , () -> profileDetails.add(OffenderProfileDetail.builder()
                    .id(new PK(this, profileType, 1))
                    .caseloadType("INST")
                    .code(code)
                    .listSequence(profileType.getListSequence())
                    .build()));
    }

    public Optional<OffenderCourtCase> getCourtCaseBy(final Long courtCaseId) {
        return courtCases == null ? Optional.empty() : courtCases.stream().filter(Objects::nonNull).filter(cc -> cc.getId().equals(courtCaseId)).findFirst();
    }

    public boolean isActive() {
        return activeFlag != null && activeFlag.equals("Y");
    }

    public List<OffenderCourtCase> getActiveCourtCases() {
        return courtCases.stream().filter(offenderCourtCase -> offenderCourtCase != null && offenderCourtCase.isActive()).collect(toUnmodifiableList());
    }

    public List<OffenderPropertyContainer> getActivePropertyContainers() {
        return propertyContainers.stream().filter(OffenderPropertyContainer::isActive).collect(toUnmodifiableList());
    }

    public List<OffenderProfileDetail> getActiveProfileDetails() {
        return profileDetails.stream()
            .filter(pd -> {
                final var profileType = pd.getId().getType();
                return profileType.getCategory().equals("PI") && (profileType.getActiveFlag().isActive() || profileType.getType().equals("RELF"));
            })
            .collect(
                Collectors.groupingBy(pd -> pd.getId().getType())
            ).entrySet().stream()
            .flatMap(pd -> pd.getValue().stream()
                .max(Comparator.comparing(op -> op.getId().getSequence()))
                .stream())
            .collect(Collectors.toList());
    }

    public int incBookingSequence() {
        if (bookingSequence == null) bookingSequence = 0;
        bookingSequence = bookingSequence + 1;
        return bookingSequence;
    }

    public ExternalMovement addExternalMovement(final ExternalMovement externalMovement) {
        externalMovement.setMovementSequence(getNextMovementSequence());
        externalMovement.setOffenderBooking(this);
        externalMovements.add(externalMovement);
        return externalMovement;
    }

    public Optional<ExternalMovement> getLastMovement() {
        return getMovementsRecentFirst().stream().findFirst();
    }


    public OffenderImprisonmentStatus setImprisonmentStatus(final OffenderImprisonmentStatus offenderImprisonmentStatus, final LocalDateTime effectiveFrom) {
        setPreviousImprisonmentStatusToInactive(effectiveFrom);
        offenderImprisonmentStatus.setImprisonStatusSeq(getNextImprisonmentStatusSequence());
        offenderImprisonmentStatus.setOffenderBooking(this);
        offenderImprisonmentStatus.makeActive();
        offenderImprisonmentStatus.setEffectiveDate(effectiveFrom.toLocalDate());
        offenderImprisonmentStatus.setEffectiveTime(effectiveFrom);
        imprisonmentStatuses.add(offenderImprisonmentStatus);
        return offenderImprisonmentStatus;
    }

    public Optional<OffenderImprisonmentStatus> getActiveImprisonmentStatus() {
        return getImprisonmentStatusesRecentFirst().stream().filter(OffenderImprisonmentStatus::isActiveLatestStatus).findFirst();
    }

    public List<ExternalMovement> getMovementsRecentFirst() {
        return externalMovements.stream()
            .sorted(Comparator.comparingLong(ExternalMovement::getMovementSequence)
                .reversed())
            .collect(Collectors.toList());
    }

    public List<OffenderImprisonmentStatus> getImprisonmentStatusesRecentFirst() {
        return imprisonmentStatuses.stream()
            .sorted(Comparator.comparingLong(OffenderImprisonmentStatus::getImprisonStatusSeq)
                .reversed())
            .collect(Collectors.toList());
    }

    public Long getNextImprisonmentStatusSequence() {
        return getImprisonmentStatusesRecentFirst().stream().findFirst().map(OffenderImprisonmentStatus::getImprisonStatusSeq).orElse(0L) + 1;
    }

    public Long getNextMovementSequence() {
        return getLastMovement().map(ExternalMovement::getMovementSequence).orElse(0L) + 1;
    }

    public void setPreviousMovementsToInactive() {
        externalMovements.stream().filter(m -> m.getActiveFlag().isActive()).forEach(m -> m.setActiveFlag(ActiveFlag.N));
    }

    public void setPreviousImprisonmentStatusToInactive(final LocalDateTime expiryTime) {
        imprisonmentStatuses.stream().filter(OffenderImprisonmentStatus::isActiveLatestStatus).forEach(m -> m.makeInactive(expiryTime));
    }

    public LegalStatus getLegalStatus() {
        return getActiveImprisonmentStatus().map(
            is -> is.getImprisonmentStatus().getLegalStatus()
        ).orElse(null);
    }

    public String getConvictedStatus() {
        return getActiveImprisonmentStatus().map(
            is -> is.getImprisonmentStatus().getConvictedStatus()
        ).orElse(null);
    }
}
