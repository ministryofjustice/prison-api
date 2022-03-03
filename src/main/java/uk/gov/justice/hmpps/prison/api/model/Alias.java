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
import java.time.LocalDate;

/**
 * Alias
 **/
@ApiModel(description = "Alias")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Alias {
    @NotBlank
    @ApiModelProperty(required = true, value = "First name of offender alias", example = "Mike")
    private String firstName;

    @ApiModelProperty(value = "Middle names of offender alias", example = "John")
    private String middleName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Last name of offender alias", example = "Smith")
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Age of Offender", example = "32")
    private Integer age;

    @NotNull
    @ApiModelProperty(required = true, value = "Date of Birth of Offender", example = "1980-02-28")
    private LocalDate dob;

    @NotBlank
    @ApiModelProperty(required = true, value = "Gender", example = "Male")
    private String gender;

    @ApiModelProperty(value = "Ethnicity", example = "Mixed: White and Black African")
    private String ethnicity;

    @ApiModelProperty(value = "Type of Alias", example = "Alias Name")
    private String nameType;

    @NotNull
    @ApiModelProperty(required = true, value = "Date of creation", example = "2019-02-15")
    private LocalDate createDate;
}
