package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "General API Error Response")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class ErrorResponse {

    @ApiModelProperty(required = true, value = "Response status code (will typically mirror HTTP status code).", example = "404", allowableValues = "400-", position = 1)
    @NotNull
    private Integer status;

    @ApiModelProperty(value = "An (optional) application-specific error code.", example = "404", position = 2)
    private Integer errorCode;

    @ApiModelProperty(required = true, value = "Concise error reason for end-user consumption.", example = "Entity Not Found", position = 3)
    @NotBlank
    private String userMessage;

    @ApiModelProperty(value = "Detailed description of problem with remediation hints aimed at application developer.", example = "Serious error in the system", position = 4)
    private String developerMessage;

    @ApiModelProperty(value = "Provision for further information about the problem (e.g. a link to a FAQ or knowledge base article).", example = "Check out this FAQ for more information", position = 5)
    private String moreInfo;

}
