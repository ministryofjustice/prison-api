package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
    @ApiModelProperty(required = true, value = "First name of offender alias")
    private String firstName;

    @ApiModelProperty(value = "Middle names of offender alias")
    private String middleName;

    @NotBlank
    @ApiModelProperty(required = true, value = "Last name of offender alias")
    private String lastName;

    @NotNull
    @ApiModelProperty(required = true, value = "Age of Offender")
    private Integer age;

    @NotNull
    @ApiModelProperty(required = true, value = "Date of Birth of Offender")
    private LocalDate dob;

    @NotBlank
    @ApiModelProperty(required = true, value = "Gender")
    private String gender;

    @ApiModelProperty(value = "Ethnicity")
    private String ethnicity;

    @NotBlank
    @ApiModelProperty(required = true, value = "Type of Alias")
    private String nameType;

    @NotNull
    @ApiModelProperty(required = true, value = "Date of creation")
    private LocalDate createDate;
}
