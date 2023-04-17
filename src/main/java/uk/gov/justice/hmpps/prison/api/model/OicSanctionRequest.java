package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Schema(description = "OicSanctionRequest")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class OicSanctionRequest {

    private String sanctionType;

    private Double compensationAmount;

    private Long sanctionMonths;

    private Long sanctionDays;

    private String comment;

    private LocalDate effectiveDate;

    private Long consecutiveSanctionSeq;

    private String status;
}
