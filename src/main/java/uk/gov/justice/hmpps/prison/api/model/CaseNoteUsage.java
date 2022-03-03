package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Case Note Type Usage
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Type Usage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteUsage {
    @Schema(required = true, description = "Offender No", example = "ZWE12A")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "Case Note Type", example = "KA")
    @NotBlank
    private String caseNoteType;

    @Schema(required = true, description = "Case Note Sub Type", example = "KS")
    @NotBlank
    private String caseNoteSubType;

    @Schema(required = true, description = "Number of case notes of this type/subtype", example = "5")
    @NotNull
    private Integer numCaseNotes;

    @Schema(required = true, description = "Last case note of this type", example = "2018-12-01T14:55:23")
    @NotNull
    private LocalDateTime latestCaseNote;

    @JsonIgnore
    @Hidden
    private Map<String, Object> additionalProperties;


}
