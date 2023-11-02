package uk.gov.justice.hmpps.prison.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.PrisonerInformation;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PrisonerStatusInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerStatusInformationRepository;
import uk.gov.justice.hmpps.prison.service.support.InmatesHelper;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class PrisonerInformationService {

    private final PrisonerStatusInformationRepository prisonerStatusInformationRepository;
    private final BookingService bookingService;
    private final InmateRepository repository;

    public PrisonerInformationService(
                                      final PrisonerStatusInformationRepository prisonerStatusInformationRepository,
                                      final BookingService bookingService,
                                      final InmateRepository repository) {

        this.prisonerStatusInformationRepository = prisonerStatusInformationRepository;
        this.bookingService = bookingService;
        this.repository = repository;
    }

    public PrisonerInformation getPrisonerInformationById(final @NotNull String offenderNo) {
        final var entity = prisonerStatusInformationRepository.getByNomsId(offenderNo).orElseThrow(() -> new EntityNotFoundException(format("Resource with id [%s] not found.", offenderNo)));

        final PrisonerInformation prisonerInformation = transform(entity);

        final var bookingIds = List.of(entity.getBookingId());
        InmatesHelper.setReleaseDate(List.of(prisonerInformation), bookingService.getBookingSentencesSummary(bookingIds));
        InmatesHelper.setCategory(List.of(prisonerInformation), repository.findAssessments(bookingIds, "CATEGORY", Set.of()));
        return prisonerInformation;
    }

    @NotNull
    private PrisonerInformation transform(final PrisonerStatusInformation entity) {
        final var prisonerInformation = PrisonerInformation.builder()
                .nomsId(entity.getNomsId())
                .givenName1(entity.getGivenName1())
                .givenName2(entity.getGivenName2())
                .lastName(entity.getLastName())
                .requestedName(entity.getRequestedName())
                .gender(entity.getGender())
                .dateOfBirth(entity.getDateOfBirth())
                .establishmentCode(entity.getEstablishmentCode())
                .establishmentName(LocationProcessor.formatLocation(entity.getEstablishmentName()))
                .bookingId(entity.getBookingId())
                .admissionDate(entity.getAdmissionDate())
                .bookingBeginDate(entity.getBookingBeginDate())
                .englishSpeaking("Y".equals(entity.getEnglishSpeakingFlag()))
                .communityStatus(format("%s %s", entity.isActive() ? "ACTIVE" : "INACTIVE", entity.getInOutStatus()))
                .build();
        prisonerInformation.setLegalStatus(ImprisonmentStatus.calcLegalStatus(entity.getBandCode(), entity.getImprisonmentStatus()));
        prisonerInformation.deriveUnitCodes(entity.getCellLocation());
        return prisonerInformation;
    }

}
