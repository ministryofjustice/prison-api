package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Predicate.not;

public class OffenderAlertTransformer {

    public static final Map<String, String> SORT_MAPPING = Map.of(
        "alertId", "sequence",
        "bookingId", "offenderBooking.bookingId",
        "alertType", "alertType",
        "alertCode", "alertCode",
        "comment", "comment",
        "dateCreated", "alertDate",
        "dateExpires", "expiryDate",
        "active", "status"
    );

    public static Alert transformForOffender(final OffenderAlert offenderAlert) {
        return transform(offenderAlert)
            .toBuilder()
            .offenderNo(offenderAlert.getOffenderBooking().getOffender().getNomsId())
            .bookingId(offenderAlert.getOffenderBooking()
                .getBookingId())
            .build();
    }

    public static Alert transformForBooking(final OffenderAlert offenderAlert) {
        return transform(offenderAlert)
            .toBuilder()
            .addedByFirstName(Optional
                .ofNullable(offenderAlert.getCreateUser())
                .map(StaffUserAccount::getStaff)
                .map(Staff::getFirstName)
                .orElse(null))
            .addedByLastName(Optional
                .ofNullable(offenderAlert.getCreateUser())
                .map(StaffUserAccount::getStaff)
                .map(Staff::getLastName)
                .orElse(null))
            .expiredByFirstName(Optional
                .ofNullable(offenderAlert.getModifyUser())
                .map(StaffUserAccount::getStaff)
                .map(Staff::getFirstName)
                .orElse(null))
            .expiredByLastName(Optional
                .ofNullable(offenderAlert.getModifyUser())
                .map(StaffUserAccount::getStaff)
                .map(Staff::getLastName)
                .orElse(null))
            .build();
    }

    private static Alert transform(final OffenderAlert offenderAlert) {
        return Alert
            .builder()
            .alertId(offenderAlert.getSequence().longValue())
            .alertCode(offenderAlert.getAlertCode())
            .alertType(offenderAlert.getAlertType())
            .alertCodeDescription(Optional
                .ofNullable(offenderAlert.getCode())
                .map(AlertCode::getDescription)
                .orElse(offenderAlert.getAlertCode()))
            .alertTypeDescription(Optional
                .ofNullable(offenderAlert.getType())
                .map(AlertType::getDescription)
                .orElse(offenderAlert.getAlertType()))
            .comment(offenderAlert.getComment())
            .expired(Optional.ofNullable(offenderAlert.getExpiryDate())
                .filter(not(date -> date.isAfter(LocalDate.now())))
                .isPresent())
            .active("ACTIVE".equals(offenderAlert.getStatus()))
            .dateCreated(offenderAlert.getAlertDate())
            .dateExpires(offenderAlert.getExpiryDate())
            .build();
    }

    public static String[] mapSortProperties(String sortProperties) {
        return Optional.ofNullable(sortProperties)
            .map(properties -> Arrays
                .stream(properties.split(","))
                .map(OffenderAlertTransformer::mapSortProperty)
                .filter(not(Objects::isNull))
                .toArray(String[]::new))
            .orElse(new String[]{});
    }

    public static String mapSortProperty(String sortProperty) {
        return SORT_MAPPING.get(sortProperty);
    }
}
