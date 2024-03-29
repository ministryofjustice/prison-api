package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;

/**
 * Profile Information
 **/
@Schema(description = "Profile Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode(of = { "type", "question"})
@ToString
@Data
public class ProfileInformation {

    @NotBlank
    @Schema(description = "Type of profile information", requiredMode = RequiredMode.NOT_REQUIRED)
    private String type;

    @NotBlank
    @Schema(description = "Profile Question", requiredMode = RequiredMode.NOT_REQUIRED)
    private String question;

    @NotBlank
    @Schema(description = "Profile Result Answer", requiredMode = RequiredMode.NOT_REQUIRED)
    private String resultValue;

    public ProfileInformation(@NotBlank String type, @NotBlank String question, @NotBlank String resultValue) {
        this.type = type;
        this.question = question;
        this.resultValue = resultValue;
    }

    public ProfileInformation() {
    }
}
