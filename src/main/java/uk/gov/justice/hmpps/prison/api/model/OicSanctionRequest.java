package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.OicSanctionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.Status;

import java.time.LocalDate;


@Schema(description = "OicSanctionRequest")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicSanctionRequest {

    @Schema(description = "Sanction Type")
    private OicSanctionCode oicSanctionCode;

    @Schema(description = "Compensation Amount")
    private Double compensationAmount;

    @Schema(description = "Sanction Days")
    private Long sanctionDays;

    @Schema(description = "Comment Text")
    private String commentText;

    @Schema(description = "Effective Date")
    @NotNull
    private LocalDate effectiveDate; //TODO confirm with John is this suspended until date as well?

    @Schema(description = "Sanction status")
    private Status status;
}
