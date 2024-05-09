package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "Title of offender alias", example = "Mr")
    private String title;

    @NotBlank
    @Schema(description = "First name of offender alias", example = "Mike", requiredMode = RequiredMode.NOT_REQUIRED)
    private String firstName;

    @Schema(description = "Middle names of offender alias", example = "John")
    private String middleName;

    @NotBlank
    @Schema(description = "Last name of offender alias", example = "Smith", requiredMode = RequiredMode.NOT_REQUIRED)
    private String lastName;

    @NotNull
    @Schema(description = "Age of Offender", example = "32", requiredMode = RequiredMode.NOT_REQUIRED)
    private Integer age;

    @NotNull
    @Schema(description = "Date of Birth of Offender", example = "1980-02-28", requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate dob;

    @NotBlank
    @Schema(description = "Gender", example = "Male", requiredMode = RequiredMode.NOT_REQUIRED)
    private String gender;

    @Schema(description = "Ethnicity", example = "Mixed: White and Black African")
    private String ethnicity;

    @Schema(description = "Type of Alias", example = "Alias Name")
    private String nameType;

    @NotNull
    @Schema(description = "Date of creation", example = "2019-02-15", requiredMode = RequiredMode.NOT_REQUIRED)
    private LocalDate createDate;

    @NotNull
    @Schema(description = "Offender ID", example = "543548")
    private Long offenderId;
}
