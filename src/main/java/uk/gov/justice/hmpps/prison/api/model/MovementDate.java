package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "A movement IN and OUT range")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class MovementDate {
    @Schema(description = "Reason for movement into prison", example = "Unconvicted Remand", requiredMode = REQUIRED)
    private String reasonInToPrison;
    private LocalDateTime dateInToPrison;
    @Schema(description = "Type of movement into prison", requiredMode = REQUIRED, allowableValues = {"ADM", "TAP"})
    private String inwardType;
    @Schema(description = "Reason for movement into prison", example = "Conditional Release (CJA91) -SH Term>1YR", requiredMode = NOT_REQUIRED)
    private String reasonOutOfPrison;
    @Schema(description = "Date this sub-period ended - if it has ended", requiredMode = NOT_REQUIRED)
    private LocalDateTime dateOutOfPrison;
    @Schema(description = "Type of movement out of prison", requiredMode = REQUIRED, allowableValues = {"REL", "TAP"})
    private String outwardType;
    @Schema(description = "The initial prison they entered during this period", requiredMode = REQUIRED, example = "MDI")
    private String admittedIntoPrisonId;
    @Schema(description = "The final prison they left during this period - if this period has ended", requiredMode = NOT_REQUIRED, example = "MDI")
    private String releaseFromPrisonId;
}
