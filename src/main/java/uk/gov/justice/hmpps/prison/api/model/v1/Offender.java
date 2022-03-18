package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;


@Schema(description = "Offender")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonAutoDetect(
        fieldVisibility = Visibility.ANY,
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE
)
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"given_name", "middle_names", "surname", "title", "suffix", "date_of_birth",
        "aliases", "gender", "nationalities", "religion", "ethnicity", "language",
        "csra", "convicted", "pnc_number", "cro_number", "imprisonment_status", "iep_level", "diet", "security_category"})
public class Offender {

    @JsonIgnore
    private String nomsId;
    @JsonIgnore
    private String singleOffenderId;
    @JsonIgnore
    private Long rootOffenderId;

    @Schema(description = "Given Name", name = "given_name", example = "JENNIFER")
    @JsonProperty("given_name")
    private String givenName;

    @Schema(description = "Middle Names", name = "middle_names", example = "ESMERALADA JANE")
    @JsonProperty("middle_names")
    private String middleNames;

    @Schema(description = "Last Name", name = "surname", example = "HALLIBUT")
    private String surname;

    @Schema(description = "Title", name = "title", example = "MR")
    private String title;

    @Schema(description = "Suffix", name = "suffix")
    private String suffix;

    @Schema(description = "Date of Birth", name = "date_of_birth", example = "1970-01-01")
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;

    @Schema(description = "List of offender’s aliases", name = "aliases")
    private List<OffenderAlias> aliases;

    @Schema(description = "Gender", name = "gender")
    private CodeDescription gender;

    @Schema(description = "Nationalities", name = "nationalities")
    private String nationalities;

    @Schema(description = "Religion", name = "religion")
    private CodeDescription religion;

    @Schema(description = "Ethnicity", name = "ethnicity")
    private CodeDescription ethnicity;

    @Schema(description = "Language", name = "language")
    private Language language;

    @Schema(description = "Cell Sharing Risk Assessment", name = "csra")
    private CodeDescription csra;

    @Schema(description = "indicates whether the offender has been convicted or is on remand", name = "convicted", example = "true")
    private boolean convicted;

    @Schema(description = "CRO Number", name = "cro_number", example = "ADF567890")
    @JsonProperty("cro_number")
    private String croNumber;

    @Schema(description = "PNC Number", name = "pnc_number", example = "96/346527V")
    @JsonProperty("pnc_number")
    private String pncNumber;

    @Schema(description = "Imprisonment Status", name = "imprisonment_status")
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;

    @Schema(description = "IEP Level", name = "iep_level")
    @JsonProperty("iep_level")
    private CodeDescription iepLevel;

    @Schema(description = "Diet", name = "diet")
    private CodeDescription diet;

    @Schema(description = "Security Categorisation", name = "security_category")
    @JsonProperty("security_category")
    private CodeDescription categorisationLevel;
}
