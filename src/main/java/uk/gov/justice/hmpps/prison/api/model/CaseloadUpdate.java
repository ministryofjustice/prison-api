package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Case Load Update
 **/
@SuppressWarnings("unused")
@Schema(description = "Caseload Update")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseloadUpdate {

    @Schema(requiredMode = REQUIRED, description = "Caseload", example = "MDI")
    @NotNull
    private String caseload;

    @Schema(requiredMode = REQUIRED, description = "Number of users enabled to access API", example = "5")
    @NotNull
    private int numUsersEnabled;
}
