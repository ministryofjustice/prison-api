package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Case Note Type Usage By Booking Id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsageByBookingId {
    @Schema(required = true, description = "Booking Id", example = "123456")
    private Integer bookingId;

    @Schema(required = true, description = "Case Note Type", example = "KA")
    private String caseNoteType;

    @Schema(required = true, description = "Case Note Sub Type", example = "KS")
    private String caseNoteSubType;

    @Schema(required = true, description = "Number of case notes of this type/subtype", example = "5")
    private Integer numCaseNotes;

    @Schema(required = true, description = "Last case note of this type", example = "2018-12-01T14:55:23")
    private LocalDateTime latestCaseNote;
}
