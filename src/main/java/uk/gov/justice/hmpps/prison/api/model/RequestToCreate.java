package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@ApiModel(description = "Represents the data required for creating a new prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToCreate {

    @ApiModelProperty(value = "The offender's PNC (Police National Computer) number.", example = "03/11999M")
    @Size(max = 20)
    @NotBlank
    private String pncNumber;

    @ApiModelProperty(required = true, value = "The offender's last name.", example = "Mark")
    @Size(max = 35)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "The offender's first name.", example = "John")
    @Size(max = 35)
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "The offender's middle name.", example = "Luke")
    @Size(max = 35)
    private String middleName1;

    @ApiModelProperty(value = "An additional middle name for the offender.", example = "Matthew")
    @Size(max = 35)
    private String middleName2;

    @ApiModelProperty(value = "A code representing the offender's title (from TITLE reference domain).", example = "MR", allowableValues = "BR,DAME,DR,FR,IMAM,LADY,LORD,MISS,MR,MRS,MS,RABBI,REV,SIR,SR")
    @Size(max = 12)
    private String title;

    @ApiModelProperty(value = "A code representing a suffix to apply to offender's name (from SUFFIX reference domain).", example = "JR", allowableValues = "I,II,III,IV,IX,V,VI,VII,VIII,JR,SR")
    @Size(max = 12)
    private String suffix;

    @ApiModelProperty(required = true, value = "The offender's date of birth. Must be specified in YYYY-MM-DD format. Current has to match YJAF allowed DOB", example = "1970-01-01")
    @NotNull
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "A code representing the offender's gender (from the SEX reference domain).", example = "M", allowableValues = "M,F,NK,NS,REF")
    @Size(max = 12)
    @NotBlank
    private String gender;

    @ApiModelProperty(value = "A code representing the offender's ethnicity (from the ETHNICITY reference domain).", example = "W1", allowableValues = "A9,B1,B2,B9,M1,M2,M3,M9,NS,O1,O2,O9,W1,W2,W3,W8,W9")
    @Size(max = 12)
    private String ethnicity;

    @ApiModelProperty(value = "The offender's CRO (Criminal Records Office) number.")
    @Size(max = 20)
    private String croNumber;

}
