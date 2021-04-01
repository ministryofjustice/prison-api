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
 * Telephone Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Telephone Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Telephone {

    @ApiModelProperty("Phone Id")
    private Long phoneId;

    @ApiModelProperty(required = true, value = "Telephone number")
    @NotBlank
    private String number;

    @ApiModelProperty(required = true, value = "Telephone type")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Telephone extension number")
    private String ext;

}
