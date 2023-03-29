package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * Physical Attributes
 **/
@Schema(description = "Physical Attributes")
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
    @Schema(description = "Gender Code", example = "M")
    private String sexCode;

    @NotBlank
    @Schema(description = "Gender", example = "Male")
    private String gender;

    @Schema(description = "Ethnicity Code", example = "W1")
    private String raceCode;

    @Schema(description = "Ethnicity", example = "White: Eng./Welsh/Scot./N.Irish/British")
    private String ethnicity;

    @Schema(description = "Height in Feet", example = "5")
    private Integer heightFeet;

    @Schema(description = "Height in Inches", example = "60")
    private Integer heightInches;

    @Schema(description = "Height in Metres (to 2dp)", example = "1.76")
    private BigDecimal heightMetres;

    @Schema(description = "Height in Centimetres", example = "176")
    private Integer heightCentimetres;

    @Schema(description = "Weight in Pounds", example = "50")
    private Integer weightPounds;

    @Schema(description = "Weight in Kilograms", example = "67")
    private Integer weightKilograms;
}
