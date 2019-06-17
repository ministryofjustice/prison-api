package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


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
@JsonPropertyOrder({"given_name", "middle_names", "surname", "title", "suffix", "date_of_birth", "given_name", "aliases", "gender", "nationalities", "religion", "ethnicity", "language", "csra", "convicted", "pnc_number", "cro_number"})
public class Offender {

    @JsonIgnore
    private String nomsId;
    @JsonIgnore
    private String singleOffenderId;
    @JsonIgnore
    private Long rootOffenderId;

    @ApiModelProperty(value = "Given Name", name = "given_name", example = "JENIFER", position = 0)
    @JsonProperty("given_name")
    private String givenName;

    @ApiModelProperty(value = "date of birth", name = "date_of_birth", example = "1970-01-01", position = 5)
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;

    @ApiModelProperty(value = "Middle Names", name = "middle_names", example = "ESMERALADA JANE", position = 1)
    @JsonProperty("middle_names")
    private String middleNames;

    @ApiModelProperty(value = "Last Name", name = "surname", example = "HALLIBUT", position = 2)
    @JsonProperty("surname")
    private String surname;

    @JsonProperty("title")
    private String title;

    @JsonProperty("suffix")
    private String suffix;

    @JsonProperty("gender")
    private CodeDescription gender;

    @JsonProperty("aliases")
    private List<OffenderAlias> aliases;

    @JsonProperty("convicted")
    private boolean convicted;

    @JsonProperty("cro_number")
    private String CRONumber;

    @JsonProperty("pnc_number")
    private String PNCNumber;

    @JsonProperty("nationalities")
    private String nationalities;

    @JsonProperty("religion")
    private CodeDescription religion;

    @JsonProperty("ethnicity")
    private CodeDescription ethnicity;

    @JsonProperty("imprisonment_status")
    private CodeDescription imprisonmentStatus;

    @JsonProperty("iep_level")
    private CodeDescription iepLevel;

    @JsonProperty("diet")
    private CodeDescription diet;

    @JsonProperty("language")
    private Language language;

    @JsonProperty("csra")
    private CodeDescription csra;

    @JsonProperty("security_category")
    private CodeDescription categorisationLevel;

    @JsonIgnore
    private List<Booking> bookings;

    @JsonIgnore
    private Image image;
}
