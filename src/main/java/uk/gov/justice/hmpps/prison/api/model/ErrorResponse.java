package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * General API Error Response
 **/
@SuppressWarnings("unused")
@Schema(description = "General API Error Response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class ErrorResponse {

    @Schema(required = true, description = "Response status code (will typically mirror HTTP status code).", example = "404")
    @NotNull
    private Integer status;

    @Schema(description = "An (optional) application-specific error code.", example = "404")
    private Integer errorCode;

    @Schema(required = true, description = "Concise error reason for end-user consumption.", example = "Entity Not Found")
    @NotBlank
    private String userMessage;

    @Schema(description = "Detailed description of problem with remediation hints aimed at application developer.", example = "Serious error in the system")
    private String developerMessage;

    @Schema(description = "Provision for further information about the problem (e.g. a link to a FAQ or knowledge base article).", example = "Check out this FAQ for more information")
    private String moreInfo;

}
