package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.math.BigDecimal;

/**
 * Physical Attributes
 **/
@ApiModel(description = "Physical Attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PhysicalAttributes {

    @NotBlank
    @ApiModelProperty(required = true, value = "Gender Code", position = 1, example = "M")
    private String sexCode;

    @NotBlank
    @ApiModelProperty(required = true, value = "Gender", position = 2, example = "Male")
    private String gender;

    @ApiModelProperty(required = true, value = "Ethnicity Code", position = 3, example = "W1")
    private String raceCode;

    @ApiModelProperty(required = true, value = "Ethnicity", position = 4, example = "White: Eng./Welsh/Scot./N.Irish/British")
    private String ethnicity;

    @ApiModelProperty(required = true, value = "Height in Feet", position = 5, example = "5")
    private Integer heightFeet;

    @ApiModelProperty(required = true, value = "Height in Inches", position = 6, example = "60")
    private Integer heightInches;

    @ApiModelProperty(required = true, value = "Height in Metres (to 2dp)", position = 7, example = "1.76")
    private BigDecimal heightMetres;

    @ApiModelProperty(required = true, value = "Height in Centimetres", position = 8, example = "176")
    private Integer heightCentimetres;

    @ApiModelProperty(required = true, value = "Weight in Pounds", position = 9, example = "50")
    private Integer weightPounds;

    @ApiModelProperty(required = true, value = "Weight in Kilograms", position = 10, example = "67")
    private Integer weightKilograms;
}
