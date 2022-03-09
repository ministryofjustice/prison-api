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
@ApiModel(description = "Telephone Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Telephone {

    @ApiModelProperty(value = "Phone Id", example = "2234232")
    private Long phoneId;

    @ApiModelProperty(required = true, value = "Telephone number", example = "0114 2345678")
    @NotBlank
    private String number;

    @ApiModelProperty(required = true, value = "Telephone type", example = "TEL")
    @NotBlank
    private String type;

    @ApiModelProperty(value = "Telephone extension number", example = "123")
    private String ext;

}
