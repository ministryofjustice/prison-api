package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "Offender damage obligation response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
public class OffenderDamageObligationResponse {

    @Schema(description = "List of offender damage obligations")
    private List<OffenderDamageObligationModel> damageObligations;
}
