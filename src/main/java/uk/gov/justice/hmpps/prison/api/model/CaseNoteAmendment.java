package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Case Note Amendment
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Amendment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseNoteAmendment {
    @Schema(requiredMode = REQUIRED, description = "Date and Time of Case Note creation", example = "2018-12-01T13:45:00")
    @NotNull
    private LocalDateTime creationDateTime;

    @Schema(requiredMode = REQUIRED, description = "Name of the user amending the case note (lastname, firstname)", example = "Smith, John")
    @NotBlank
    private String authorName;

    @Schema(requiredMode = REQUIRED, description = "Additional Case Note Information", example = "Some Additional Text")
    @NotBlank
    private String additionalNoteText;
}
