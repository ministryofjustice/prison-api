package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Represents the data required for creating a new prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToCreate {

    @Schema(description = "The offender's PNC (Police National Computer) number.", example = "03/11999M")
    @Size(max = 20)
    @Pattern(regexp = "^^([0-9]{2}|[0-9]{4})/[0-9]+[a-zA-Z]$", message = "PNC is not valid")
    private String pncNumber;

    @Schema(required = true, description = "The offender's last name.", example = "Mark")
    @Size(max = 35)
    @NotBlank
    @Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Last name is not valid")
    private String lastName;

    @Schema(required = true, description = "The offender's first name.", example = "John")
    @Size(max = 35)
    @NotBlank
    @Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "First name is not valid")
    private String firstName;

    @Schema(description = "The offender's middle name.", example = "Luke")
    @Size(max = 35)
    @Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name is not valid")
    private String middleName1;

    @Schema(description = "An additional middle name for the offender.", example = "Matthew")
    @Size(max = 35)
    @Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name 2 is not valid")
    private String middleName2;

    @Schema(description = "A code representing the offender's title (from TITLE reference domain).", example = "MR", allowableValues = {"BR","DAME","DR","FR","IMAM","LADY","LORD","MISS","MR","MRS","MS","RABBI","REV","SIR","SR"})
    @Size(max = 12)
    private String title;

    @Schema(description = "A code representing a suffix to apply to offender's name (from SUFFIX reference domain).", example = "JR", allowableValues = {"I","II","III","IV","IX","V","VI","VII","VIII","JR","SR"})
    @Size(max = 12)
    private String suffix;

    @Schema(required = true, description = "The offender's date of birth. Must be specified in YYYY-MM-DD format. Range allowed is 16-110 years", example = "1970-01-01")
    @NotNull
    private LocalDate dateOfBirth;

    @Schema(required = true, description = "A code representing the offender's gender (from the SEX reference domain).", example = "M", allowableValues = {"M","F","NK","NS","REF"})
    @Size(max = 12)
    @NotBlank
    private String gender;

    @Schema(description = "A code representing the offender's ethnicity (from the ETHNICITY reference domain).", example = "W1", allowableValues = {"A9","B1","B2","B9","M1","M2","M3","M9","NS","O1","O2","O9","W1","W2","W3","W8","W9"})
    @Size(max = 12)
    private String ethnicity;

    @Schema(description = "The offender's CRO (Criminal Records Office) number.")
    @Size(max = 20)
    private String croNumber;

}
