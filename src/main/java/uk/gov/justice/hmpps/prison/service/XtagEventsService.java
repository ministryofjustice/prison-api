package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.XtagEventsRepository;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEventsTransformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class XtagEventsService {

    private final XtagEventsRepository xtagEventsRepository;
    private final OffenderEventsTransformer offenderEventsTransformer;
    private final MovementsService movementsService;
    private final OffenderRepository offenderRepository;
    private final OffenderBookingRepository offenderBookingRepository;


    @PreAuthorize("hasRole('PRISON_OFFENDER_EVENTS')")
    public List<OffenderEvent> findAll(final OffenderEventsFilter oeFilter) {
        return xtagEventsRepository.findAll(fudgedXtagFilterOf(oeFilter)).stream()
                .map(offenderEventsTransformer::offenderEventOf)
                .map(this::addAdditionalEventData)
                .toList();
    }

    private OffenderEventsFilter fudgedXtagFilterOf(final OffenderEventsFilter oeFilter) {
        // Xtag events are in British Summer Time all year round at rest in Oracle.
        // So we have to compensate when filtering by date. The Nomis data set
        // is stored at rest as Europe/London and so is affected by daylight savings.
        return oeFilter.toBuilder()
                .from(asUtcPlusOne(oeFilter.getFrom()))
                .to(asUtcPlusOne(oeFilter.getTo()))
                .build();
    }

    static LocalDateTime asUtcPlusOne(final LocalDateTime localDateTime) {
        if (ZoneId.of("Europe/London").getRules().isDaylightSavings(localDateTime.toInstant(ZoneOffset.UTC))) {
            return localDateTime;
        }
        return localDateTime.plusHours(1L);
    }


    private OffenderEvent addAdditionalEventData(final OffenderEvent oe) {
        switch (oe.getEventType()) {
            case "OFFENDER_DETAILS-CHANGED", "OFFENDER_ALIAS-CHANGED", "OFFENDER-UPDATED" -> {
                final var nomsId = offenderRepository.findById(oe.getOffenderId()).map(Offender::getNomsId)
                    .orElse(null);
                oe.setOffenderIdDisplay(nomsId);
            }
            case "BED_ASSIGNMENT_HISTORY-INSERTED", "OFFENDER_MOVEMENT-DISCHARGE", "OFFENDER_MOVEMENT-RECEPTION", "CONFIRMED_RELEASE_DATE-CHANGED", "SENTENCE_DATES-CHANGED" -> {
                final var nomsId = offenderBookingRepository.findById(oe.getBookingId()).map(b -> b.getOffender().getNomsId())
                    .orElse(null);
                oe.setOffenderIdDisplay(nomsId);
            }
            case "EXTERNAL_MOVEMENT_RECORD-INSERTED" -> movementsService.getMovementByBookingIdAndSequence(oe.getBookingId(), oe.getMovementSeq().intValue())
                .ifPresent(movement -> {
                    oe.setOffenderIdDisplay(movement.getOffenderNo());
                    oe.setFromAgencyLocationId(movement.getFromAgency());
                    oe.setToAgencyLocationId(movement.getToAgency());
                    oe.setDirectionCode(movement.getDirectionCode());
                    oe.setMovementDateTime(movement.getMovementTime() != null && movement.getMovementDate() != null ? movement.getMovementTime().atDate(movement.getMovementDate()) : null);
                    oe.setMovementType(movement.getMovementType());
                });
        }
        return oe;
    }
}
