package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@SuppressWarnings("unused")
@Schema(description = "Restrictive Patient details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class RestrictivePatient {
    @Schema(requiredMode = REQUIRED, description = "Prison where the offender is support by POM")
    @NotNull
    private Agency supportingPrison;

    @Schema(requiredMode = REQUIRED, description = "Hospital where the offender is currently located")
    @NotNull
    private Agency dischargedHospital;

    @Schema(requiredMode = REQUIRED, description = "Date Discharged")
    @NotNull
    private LocalDate dischargeDate;

    @Schema(description = "Discharge details")
    private String dischargeDetails;

}
