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

@Data
@Schema(description = "Offender out today details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffenderOutTodayDto {

    @NotBlank
    @Schema(required = true, description = "Offender Unique Reference")
    private String offenderNo;

    @NotBlank
    @Schema(required = true)
    private LocalDate dateOfBirth;

    @Schema(description = "Reason for out movement")
    private String reasonDescription;

    @NotBlank
    @Schema(required = true)
    private LocalTime timeOut;

    @NotBlank
    @Schema(required = true)
    private String firstName;

    @NotBlank
    @Schema(required = true)
    private String lastName;
}
