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
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * Profile Information
 **/
@Schema(description = "Profile Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "type", "question"})
@ToString
@Data
public class ProfileInformation {

    @NotBlank
    @Schema(required = true, description = "Type of profile information")
    private String type;

    @NotBlank
    @Schema(required = true, description = "Profile Question")
    private String question;

    @NotBlank
    @Schema(required = true, description = "Profile Result Answer")
    private String resultValue;

}
