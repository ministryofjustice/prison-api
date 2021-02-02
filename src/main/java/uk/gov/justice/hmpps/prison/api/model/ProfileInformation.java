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

/**
 * Profile Information
 **/
@ApiModel(description = "Profile Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "type", "question"})
@ToString
@Data
public class ProfileInformation {

    @NotBlank
    @ApiModelProperty(required = true, value = "Type of profile information")
    private String type;

    @NotBlank
    @ApiModelProperty(required = true, value = "Profile Question")
    private String question;

    @NotBlank
    @ApiModelProperty(required = true, value = "Profile Result Answer")
    private String resultValue;

}
