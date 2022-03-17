package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "Offender restrictions")
@Data
@AllArgsConstructor
public class OffenderRestrictions {
    @Schema(description = "Booking id for offender")
    private Long bookingId;

    @Schema(description = "Offender restrictions")
    final List<OffenderRestriction> offenderRestrictions;
}
