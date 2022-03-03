package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "Offender contacts")
@Data
@AllArgsConstructor
public class OffenderContacts {
    @Schema(description = "Offender contacts")
    final List<OffenderContact> offenderContacts;
}
