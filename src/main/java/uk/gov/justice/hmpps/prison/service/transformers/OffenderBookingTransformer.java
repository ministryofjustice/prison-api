package uk.gov.justice.hmpps.prison.service.transformers;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.PrisonerBookingSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.getAge;

@Component
@AllArgsConstructor
public class OffenderBookingTransformer {

    private final Clock clock;

    public static final Map<String, String> SORT_MAPPING = Map.of(
        "bookingId", "bookingId",
        "offenderNo", "offender.nomsId",
        "prisonId", "agencyLocation.id",
        "lastName", "offender.lastName",
        "firstName", "offender.firstName",
        "dateOfBirth", "offender.birthDate"
    );

    public PrisonerBookingSummary transform(final OffenderBooking offenderBooking, final boolean legalInfo, final boolean imageId) {
        final var bookingSummaryBuilder = PrisonerBookingSummary.builder()
            .bookingId(offenderBooking.getBookingId())
            .bookingNo(offenderBooking.getBookNumber())
            .offenderNo(offenderBooking.getOffender().getNomsId())
            .dateOfBirth(offenderBooking.getOffender().getBirthDate())
            .age(getAge(offenderBooking.getOffender().getBirthDate(), LocalDate.now(clock)))
            .lastName(offenderBooking.getOffender().getLastName())
            .firstName(offenderBooking.getOffender().getFirstName())
            .middleName(offenderBooking.getOffender().getMiddleNames())
            .agencyId(offenderBooking.getLocation().getId())
            .assignedLivingUnitId(offenderBooking.getAssignedLivingUnit() != null ? offenderBooking.getAssignedLivingUnit().getLocationId() : null)
            .assignedLivingUnitDesc(offenderBooking.getAssignedLivingUnit() != null ? offenderBooking.getAssignedLivingUnit().getDescription() : null);

        if (legalInfo) {
            offenderBooking.getActiveImprisonmentStatus().ifPresent(imp -> {
                final var imprisonmentStatus = imp.getImprisonmentStatus();
                bookingSummaryBuilder
                    .convictedStatus(imprisonmentStatus.getConvictedStatus())
                    .legalStatus(imprisonmentStatus.getLegalStatus())
                    .imprisonmentStatus(imprisonmentStatus.getStatus());
            });

        }
        if (imageId) {
            bookingSummaryBuilder
                .facialImageId(offenderBooking.getLatestFaceImage().map(OffenderImage::getId).orElse(null));
        }

        return bookingSummaryBuilder.build();
    }

    public static String mapSortProperty(String sortProperty) {
        return SORT_MAPPING.get(sortProperty);
    }
}
