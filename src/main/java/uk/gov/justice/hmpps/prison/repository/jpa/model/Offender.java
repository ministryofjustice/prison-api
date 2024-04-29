package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.springframework.data.annotation.CreatedDate;
import uk.gov.justice.hmpps.prison.api.model.SignificantMovement;
import uk.gov.justice.hmpps.prison.api.model.PrisonPeriod;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInPrisonSummary;
import uk.gov.justice.hmpps.prison.api.model.TransferDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier.OffenderIdentifierPK;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity.ETHNICITY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.SEX;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Suffix.SUFFIX;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Title.TITLE;

@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "OFFENDERS")
@With
public class Offender extends AuditableEntity {

    @SequenceGenerator(name = "OFFENDER_ID", sequenceName = "OFFENDER_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_ID")
    @Id
    @Column(name = "OFFENDER_ID", nullable = false)
    private Long id;

    @Column(name = "OFFENDER_ID_DISPLAY", nullable = false)
    private String nomsId;

    @Column(name = "ID_SOURCE_CODE", nullable = false)
    @Default
    private String idSourceCode = "SEQ";

    @Column(name = "NAME_SEQUENCE")
    @Default
    private String nameSequence = "1234";

    @Column(name = "CASELOAD_TYPE")
    @Default
    private String caseloadType = "INST";

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "MIDDLE_NAME_2")
    private String middleName2;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_OFFENDER_ID", updatable = false, insertable = false)
    @Exclude
    private Offender rootOffender;

    @OneToMany(mappedBy = "rootOffender", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Default
    @Exclude
    @Getter(AccessLevel.PROTECTED) // should be private but need to go via the getter to trigger hibernate load
    @Setter(AccessLevel.NONE)
    private List<OffenderBooking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Default
    @Exclude
    private List<OffenderIdentifier> identifiers = new ArrayList<>();

    @OneToMany(mappedBy = "rootOffenderId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Default
    @Exclude
    @Getter(AccessLevel.PROTECTED) // should be private but need to go via the getter to trigger hibernate load
    @Setter(AccessLevel.NONE)
    private List<OffenderIdentifier> allIdentifiersInternal = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SEX + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "SEX_CODE", referencedColumnName = "code", nullable = false))
    })
    @Exclude
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + ETHNICITY + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "RACE_CODE", referencedColumnName = "code"))
    })
    @Exclude
    private Ethnicity ethnicity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TITLE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "TITLE", referencedColumnName = "code"))
    })
    @Exclude
    private Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SUFFIX + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "SUFFIX", referencedColumnName = "code"))
    })
    @Exclude
    private Suffix suffix;

    @Column(name = "CREATE_DATE", nullable = false)
    @CreatedDate
    @Default
    private LocalDate createDate = LocalDate.now();

    @Column(name = "LAST_NAME_KEY", nullable = false)
    private String lastNameKey;

    @Column(name = "LAST_NAME_SOUNDEX")
    private String lastNameSoundex;

    @Column(name = "LAST_NAME_ALPHA_KEY")
    private String lastNameAlphaKey;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Default
    @Exclude
    private List<OffenderAddress> addresses = new ArrayList<>();

    public Optional<OffenderIdentifier> getLatestIdentifierOfType(final String type) {
        final var offenderIdentifiers = mapOfIdentifiers().get(type);
        if (offenderIdentifiers != null) {
            return offenderIdentifiers.stream().max(Comparator.comparing(id -> id.getOffenderIdentifierPK().getOffenderIdSeq()));
        } else {
            return Optional.empty();
        }
    }

    public List<OffenderIdentifier> getLatestIdentifiers() {
        return mapOfIdentifiers()
            .entrySet().stream()
            .flatMap(pd -> pd.getValue().stream()
                .max(Comparator.comparing(id -> id.getOffenderIdentifierPK().getOffenderIdSeq()))
                .stream())
            .toList();
    }

    private Map<String, List<OffenderIdentifier>> mapOfIdentifiers() {
        return identifiers.stream().collect(Collectors.groupingBy(OffenderIdentifier::getIdentifierType));
    }

    public Optional<String> getPnc() {
        return getLatestIdentifierOfType("PNC").map(OffenderIdentifier::getIdentifier);
    }

    public Optional<String> getCro() {
        return getLatestIdentifierOfType("CRO").map(OffenderIdentifier::getIdentifier);
    }

    public OffenderIdentifier addIdentifier(final String type, final String value) {
        final var latestSeq = identifiers.stream().max(Comparator.comparing(id -> id.getOffenderIdentifierPK().getOffenderIdSeq())).map(id -> id.getOffenderIdentifierPK().getOffenderIdSeq()).orElse(0L);
        final var offenderIdentifier = OffenderIdentifier.builder()
            .offender(this)
            .offenderIdentifierPK(new OffenderIdentifierPK(getId(), latestSeq + 1))
            .identifierType(type)
            .identifier(value)
            .issuedDate(LocalDate.now())
            .rootOffenderId(getRootOffenderId())
            .caseloadType("INST")
            .build();
        identifiers.add(offenderIdentifier);
        return offenderIdentifier;
    }

    public List<ExternalMovement> getAllMovements() {
        final var externalMovements = getAllBookings().stream().flatMap(b -> b.getExternalMovements().stream());
        return externalMovements.sorted(Comparator.comparing(ExternalMovement::getMovementSequence)).toList();
    }

    public void addBooking(final OffenderBooking booking) {
        booking.setOffender(this);
        booking.setRootOffender(this.rootOffender);
        getAllBookings().add(booking);
        getAllBookings().forEach(OffenderBooking::incBookingSequence);
    }

    private boolean isTransferOutOrAdmissionThatBecameATransfer(ExternalMovement movement) {
        if (movement.getMovementType() == null)
            return false;

        if (isTransferOut(movement))
            return true;

        if (movement.getMovementReason() == null)
            return false;

        // treat transfer via court or TAP as transfers
        return movement.getMovementType().getCode().equals("ADM") && List.of("TRNTAP", "TRNCRT").contains(movement.getMovementReason().getCode());
    }

    private boolean isAdmission(ExternalMovement movement) {
        if (movement.getMovementType() == null)
            return false;

        return movement.getMovementType().getCode().equals("ADM");
    }

    private boolean isTransferOut(ExternalMovement movement) {
        if (movement.getMovementType() == null)
            return false;

        return movement.getMovementType().getCode().equals("TRN") && MovementDirection.OUT.equals(movement.getMovementDirection());
    }

    private boolean isTapOrCourtOut(ExternalMovement movement) {
        if (movement.getMovementType() == null)
            return false;

        if (MovementDirection.IN.equals(movement.getMovementDirection()))
            return false;

        return List.of("CRT", "TAP").contains(movement.getMovementType().getCode());
    }

    public PrisonerInPrisonSummary getPrisonerInPrisonSummary() {
        List<ExternalMovement> allMovements = getAllMovements();
        final var movementMap = allMovements.stream()
            .filter(f -> f.getMovementType() != null && List.of("ADM", "REL", "TAP").contains(f.getMovementType().getCode()))
            .collect(Collectors.groupingBy(ExternalMovement::getOffenderBooking));

        final var transferMap = allMovements.stream()
            .filter(this::isTransferOutOrAdmissionThatBecameATransfer)
            .collect(Collectors.groupingBy(ExternalMovement::getOffenderBooking));

        final var admissionsMap = allMovements.stream()
            .filter(this::isAdmission)
            .collect(Collectors.groupingBy(ExternalMovement::getOffenderBooking));

        final var tapAndCourtOutMap = allMovements.stream()
            .filter(this::isTapOrCourtOut)
            .collect(Collectors.groupingBy(ExternalMovement::getOffenderBooking));

        final var summary = PrisonerInPrisonSummary.builder()
            .prisonerNumber(getNomsId())
            .prisonPeriod(movementMap.entrySet().stream()
                .filter(p -> !p.getValue().isEmpty())
                .map(e ->
                    PrisonPeriod.builder()
                        .bookingId(e.getKey().getBookingId())
                        .bookNumber(e.getKey().getBookNumber())
                        .prisons(getAdmissionPrisons(e.getValue()))
                        .movementDates(buildMovements(e.getValue()))
                        .transfers(buildTransfers(
                                transferMap.getOrDefault(e.getKey(), List.of()),
                                admissionsMap.getOrDefault(e.getKey(), List.of()),
                                tapAndCourtOutMap.getOrDefault(e.getKey(), List.of())
                            )
                        )
                        .build())
                .collect(toList())
            )
            .build();

        summary.getPrisonPeriod().forEach(pp -> {
                final var noAdmission = pp.getMovementDates().stream().noneMatch(m -> "ADM".equals(m.getInwardType()));
                pp.setEntryDate(pp.getMovementDates().stream()
                    // if there is no admission then use the first movement as the entry date
                    .filter(m -> noAdmission || "ADM".equals(m.getInwardType()))
                    .findFirst()
                    // if there is no date into prison then we fall back onto the date out of prison instead
                    .map(m -> (m.getDateInToPrison() != null ? m.getDateInToPrison() : m.getDateOutOfPrison()))
                    .orElse(null));

                pp.setReleaseDate(pp.getLastMovement()
                    .filter(m -> "REL".equals(m.getOutwardType()))
                    .map(SignificantMovement::getDateOutOfPrison).orElse(null));
            }
        );

        // sort bookings by entry date
        summary.setPrisonPeriod(summary.getPrisonPeriod().stream().sorted(Comparator.comparing(PrisonPeriod::getEntryDate)).toList());
        return summary;
    }

    private List<SignificantMovement> buildMovements(final List<ExternalMovement> externalMovements) {
        final var movements = new ArrayList<SignificantMovement>();

        SignificantMovement lastMovement = null;
        for (ExternalMovement externalMovement : externalMovements) {
            final var movementRange = createMovementRange(externalMovement, lastMovement);
            if (movementRange.isPresent()) {
                movements.add(movementRange.get());
                lastMovement = movementRange.get();
            }
        }

        return movements;
    }

    private List<TransferDetail> buildTransfers(final List<ExternalMovement> transferMovements,
                                                final List<ExternalMovement> allAdmissions,
                                                final List<ExternalMovement> allTapAndCourtOutMovements) {
        return transferMovements.stream().map(transfer -> {
            // a normal transfer out - so find the companion admission
            if (isTransferOut(transfer)) {
                var admission = findAdmissionForTransferOut(transfer, allAdmissions);
                return TransferDetail
                    .builder()
                    .dateOutOfPrison(transfer.getMovementTime())
                    .dateInToPrison(admission.map(ExternalMovement::getMovementTime).orElse(null))
                    .transferReason(transfer.getMovementReason().getDescription())
                    .fromPrisonId(Optional.ofNullable(transfer.getFromAgency()).map(AgencyLocation::getId).orElse(null))
                    .toPrisonId(admission.map(ExternalMovement::getToAgency).map(AgencyLocation::getId).orElse(null))
                    .build();
            }
            // an admission (which will be due to transfer via court or TAP) - so find the companion release to court/tap
            else {
                var courtOrTapMovement = findMovementToCourtOrTapForAdmissionIn(transfer, allTapAndCourtOutMovements);
                return TransferDetail
                    .builder()
                    .dateOutOfPrison(courtOrTapMovement.map(ExternalMovement::getMovementTime).orElse(null))
                    .dateInToPrison(transfer.getMovementTime())
                    .transferReason(transfer.getMovementReason().getDescription())
                    .fromPrisonId(Optional.ofNullable(transfer.getFromAgency()).map(AgencyLocation::getId).orElse(null))
                    .toPrisonId(Optional.ofNullable(transfer.getToAgency()).map(AgencyLocation::getId).orElse(null))
                    .build();
            }

        }).collect(toList());
    }

    private Optional<ExternalMovement> findAdmissionForTransferOut(ExternalMovement transferOut, List<ExternalMovement> admissionInMovements) {
        return admissionInMovements.stream().filter(admission -> admission.getMovementSequence() == transferOut.getMovementSequence() + 1).findFirst();
    }

    private Optional<ExternalMovement> findMovementToCourtOrTapForAdmissionIn(ExternalMovement admission, List<ExternalMovement> courtOrTapMovementsOut) {
        return courtOrTapMovementsOut.stream().filter(movement -> movement.getMovementSequence() == admission.getMovementSequence() - 1).findFirst();
    }

    private Optional<SignificantMovement> createMovementRange(final ExternalMovement m, final SignificantMovement md) {

        SignificantMovement newMovement = md;
        boolean newEntry = false;
        if (md == null || (md.getDateInToPrison() != null && md.getDateOutOfPrison() != null)) {
            // new entry
            newMovement = SignificantMovement.builder().build();
            newEntry = true;
        }
        if ("ADM".equals(m.getMovementType().getCode()) && newMovement.getDateInToPrison() == null) {
            inward(m, newMovement);

        } else if ("REL".equals(m.getMovementType().getCode()) && newMovement.getDateOutOfPrison() == null) {
            outward(m, newMovement);

        } else if ("TAP".equals(m.getMovementType().getCode())) {
            if (newMovement.getDateInToPrison() != null && m.getMovementDirection() == MovementDirection.OUT) {
                outward(m, newMovement);
            } else if (m.getMovementDirection() == MovementDirection.IN) {
                inward(m, newMovement);
            }
        }

        return newEntry ? Optional.of(newMovement) : Optional.empty();
    }

    private List<String> getAdmissionPrisons(List<ExternalMovement> externalMovements) {
        return externalMovements.stream()
            .filter(m -> m.getMovementType().getCode().equals("ADM"))
            .map(m -> m.getToAgency().getId())
            .distinct()
            .toList();
    }

    private void outward(final ExternalMovement m, final SignificantMovement md) {
        md.setDateOutOfPrison(m.getMovementTime());
        md.setReasonOutOfPrison(m.getMovementReason().getDescription());
        md.setOutwardType(m.getMovementType().getCode());
        md.setReleaseFromPrisonId(Optional.ofNullable(m.getFromAgency()).map(AgencyLocation::getId).orElse(null));
    }

    private void inward(final ExternalMovement m, final SignificantMovement md) {
        md.setDateInToPrison(m.getMovementTime());
        md.setReasonInToPrison(m.getMovementReason().getDescription());
        md.setInwardType(m.getMovementType().getCode());
        md.setAdmittedIntoPrisonId(Optional.ofNullable(m.getToAgency()).map(AgencyLocation::getId).orElse(null));
    }

    public String getMiddleNames() {
        return StringUtils.trimToNull(StringUtils.trimToEmpty(middleName) + " " + StringUtils.trimToEmpty(middleName2));
    }

    public List<OffenderBooking> getAllBookings() {
        return rootOffender.getBookings();
    }

    public List<OffenderIdentifier> getAllIdentifiers() {
        return rootOffender.getAllIdentifiersInternal();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Offender offender = (Offender) o;

        return Objects.equals(getId(), offender.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
