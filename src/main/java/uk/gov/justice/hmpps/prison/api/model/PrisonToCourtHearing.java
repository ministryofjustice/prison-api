package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Represents the data required to schedule a prison to court hearing for an offender.")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrisonToCourtHearing {

    @Schema(required = true, description = "The prison (agency code) where the offender will be moved from.", example = "LEI")
    @NotBlank(message = "The from prison location must be provided.")
    @Size(max = 6, message = "From location must be a maximum of 6 characters.")
    private String fromPrisonLocation;

    @Schema(required = true, description = "The court (agency code) where the offender will moved to.", example = "LEEDCC")
    @NotBlank(message = "The court location to be moved to must be provided.")
    @Size(max = 6, message = "To location must be a maximum of 6 characters.")
    private String toCourtLocation;

    @Schema(required = true, description = "The future date and time of the court hearing in Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2020-02-28T14:40:00")
    @NotNull(message = "The future court hearing date time must be provided.")
    private LocalDateTime courtHearingDateTime;

    @Schema(description = "Any comments related to the court case.", example = "Restricted access to parking level.")
    @Size(max = 240, message = "Comment text must be a maximum of 240 characters.")
    private String comments;
}
