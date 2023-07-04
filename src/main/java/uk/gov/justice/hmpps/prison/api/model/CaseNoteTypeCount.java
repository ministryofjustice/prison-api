package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Case Note Type Counts By Booking Id, type and sub type")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CaseNoteTypeCount {
    @Schema(requiredMode = REQUIRED, description = "Booking Id", example = "12345678")
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Case Note Type", example = "POS")
    private String caseNoteType;

    @Schema(requiredMode = REQUIRED, description = "Case Note Sub Type", example = "IEP_ENC")
    private String caseNoteSubType;

    @Schema(requiredMode = REQUIRED, description = "Number of case notes of this type and subtype", example = "5")
    private Long numCaseNotes;
}
