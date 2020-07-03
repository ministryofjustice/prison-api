package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;


@ApiModel(description = "Offender")
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

    @ApiModelProperty(value = "Given Name", name = "given_name", example = "JENNIFER", position = 0)
    @JsonProperty("given_name")
    private String givenName;

    @ApiModelProperty(value = "Middle Names", name = "middle_names", example = "ESMERALADA JANE", position = 1)
    @JsonProperty("middle_names")
    private String middleNames;

    @ApiModelProperty(value = "Last Name", name = "surname", example = "HALLIBUT", position = 2)
    private String surname;

    @ApiModelProperty(value = "Title", name = "title", example = "MR", position = 3)
    private String title;

    @ApiModelProperty(value = "Suffix", name = "suffix", position = 4)
    private String suffix;

    @ApiModelProperty(value = "Date of Birth", name = "date_of_birth", example = "1970-01-01", position = 5)
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;

    @ApiModelProperty(value = "List of offender’s aliases", name = "aliases", position = 6)
    private List<OffenderAlias> aliases;

    @ApiModelProperty(value = "Gender", name = "gender", position = 7)
    private CodeDescription gender;

    @ApiModelProperty(value = "Nationalities", name = "nationalities", position = 8)
    private String nationalities;

    @ApiModelProperty(value = "Religion", name = "religion", position = 9)
    private CodeDescription religion;

    @ApiModelProperty(value = "Ethnicity", name = "ethnicity", position = 10)
    private CodeDescription ethnicity;

    @ApiModelProperty(value = "Language", name = "language", position = 11)
    private Language language;

    @ApiModelProperty(value = "Cell Sharing Risk Assessment", name = "csra", position = 12)
    private CodeDescription csra;

    @ApiModelProperty(value = "indicates whether the offender has been convicted or is on remand", name = "convicted", example = "true", position = 13)
    private boolean convicted;

    @ApiModelProperty(value = "CRO Number", name = "cro_number", example = "ADF567890", position = 14)
    @JsonProperty("cro_number")
    private String croNumber;

    @ApiModelProperty(value = "PNC Number", name = "pnc_number", example = "96/346527V", position = 15)
    @JsonProperty("pnc_number")
    private String pncNumber;

    @ApiModelProperty(value = "Imprisonment Status", name = "imprisonment_status", position = 16)
    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;

    @ApiModelProperty(value = "IEP Level", name = "iep_level", position = 17)
    @JsonProperty("iep_level")
    private CodeDescription iepLevel;

    @ApiModelProperty(value = "Diet", name = "diet", position = 18)
    private CodeDescription diet;

    @ApiModelProperty(value = "Security Categorisation", name = "security_category", position = 19)
    @JsonProperty("security_category")
    private CodeDescription categorisationLevel;
}
