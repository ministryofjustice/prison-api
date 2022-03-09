package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
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
@Schema(description = "Alias")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class Alias {
    @NotBlank
    @Schema(required = true, description = "First name of offender alias", example = "Mike")
    private String firstName;

    @Schema(description = "Middle names of offender alias", example = "John")
    private String middleName;

    @NotBlank
    @Schema(required = true, description = "Last name of offender alias", example = "Smith")
    private String lastName;

    @NotNull
    @Schema(required = true, description = "Age of Offender", example = "32")
    private Integer age;

    @NotNull
    @Schema(required = true, description = "Date of Birth of Offender", example = "1980-02-28")
    private LocalDate dob;

    @NotBlank
    @Schema(required = true, description = "Gender", example = "Male")
    private String gender;

    @Schema(description = "Ethnicity", example = "Mixed: White and Black African")
    private String ethnicity;

    @Schema(description = "Type of Alias", example = "Alias Name")
    private String nameType;

    @NotNull
    @Schema(required = true, description = "Date of creation", example = "2019-02-15")
    private LocalDate createDate;
}
