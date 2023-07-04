package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@Schema(description = "Offender out today details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffenderOutTodayDto {

    @NotBlank
    @Schema(requiredMode = REQUIRED, description = "Offender Unique Reference")
    private String offenderNo;

    @NotBlank
    @Schema(requiredMode = REQUIRED)
    private LocalDate dateOfBirth;

    @Schema(description = "Reason for out movement")
    private String reasonDescription;

    @NotBlank
    @Schema(requiredMode = REQUIRED)
    private LocalTime timeOut;

    @NotBlank
    @Schema(requiredMode = REQUIRED)
    private String firstName;

    @NotBlank
    @Schema(requiredMode = REQUIRED)
    private String lastName;
}
