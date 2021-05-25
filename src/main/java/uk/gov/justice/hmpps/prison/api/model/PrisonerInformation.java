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
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@ApiModel(description = "Prisoner Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonerInformation implements CategoryCodeAware, ReleaseDateAware {

    @Id
    @ApiModelProperty(value = "Offender Identifier", example = "A1234AA", required = true, position = 1)
    private String nomsId;

    @ApiModelProperty(value = "Establishment Code for prisoner", example = "MDI", required = true, position = 2)
    private String establishmentCode;

    @ApiModelProperty(value = "Booking Id (Internal)", example = "1231232", required = true, position = 3)
    private Long bookingId;

    @ApiModelProperty(value = "Given Name 1", example = "John", required = true, position = 4)
    private String givenName1;

    @ApiModelProperty(value = "Given Name 2", example = "Luke", position = 5)
    private String givenName2;

    @ApiModelProperty(value = "Last Name", example = "Smith", required = true, position = 6)
    private String lastName;

    @ApiModelProperty(value = "Requested Name", example = "Dave", position = 7)
    private String requestedName;

    @ApiModelProperty(value = "Date of Birth", example = "1970-05-01", required = true, position = 8)
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Gender", example = "Male", required = true, position = 9)
    private String gender;

    @ApiModelProperty(value = "Indicated that is English speaking", example = "true", required = true, position = 10)
    private boolean englishSpeaking;

    @ApiModelProperty(value = "Level 1 Location Unit Code", example = "A", position = 11)
    private String unitCode1;

    @ApiModelProperty(value = "Level 2 Location Unit Code", example = "2", position = 12)
    private String unitCode2;

    @ApiModelProperty(value = "Level 3 Location Unit Code", example = "003", position = 13)
    private String unitCode3;

    @ApiModelProperty(value = "Date Prisoner booking was initial made", example = "2017-05-01", position = 14)
    private LocalDate bookingBeginDate;

    @ApiModelProperty(value = "Date of admission into this prison", example = "2019-06-01", position = 15)
    private LocalDate admissionDate;

    @ApiModelProperty(value = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
            "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2021-04-12", position = 16)
    private LocalDate releaseDate;

    @ApiModelProperty(value = "Category of this prisoner", example = "C", position = 17)
    private String categoryCode;

    @ApiModelProperty(value = "Status of prisoner in community", required = true, example = "ACTIVE IN", allowableValues = "ACTIVE IN,ACTIVE OUT", position = 18)
    private String communityStatus;

    @ApiModelProperty(value = "Legal Status", example = "REMAND", position = 19)
    private LegalStatus legalStatus;

    public void deriveUnitCodes(final String cellLocation) {
        if (StringUtils.isNotBlank(cellLocation)) {
            final var levels = StringUtils.split(cellLocation, "-");
            if (levels.length > 1) {
                setUnitCode1(levels[1]);
            }
            if (levels.length > 2) {
                setUnitCode2(levels[2]);
            }
            if (levels.length > 3) {
                setUnitCode3(levels[3]);
            }
            if (levels.length > 4) {
                setUnitCode3(getUnitCode3() + "-" + levels[4]);
            }
        }
    }
}
