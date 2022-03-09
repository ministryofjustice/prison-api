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
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Schema(description = "Prisoner Information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString
public class PrisonerInformation implements CategoryCodeAware, ReleaseDateAware {

    @Id
    @Schema(description = "Offender Identifier", example = "A1234AA", required = true)
    private String nomsId;

    @Schema(description = "Establishment Code for prisoner", example = "MDI", required = true)
    private String establishmentCode;

    @Schema(description = "Booking Id (Internal)", example = "1231232", required = true)
    private Long bookingId;

    @Schema(description = "Given Name 1", example = "John", required = true)
    private String givenName1;

    @Schema(description = "Given Name 2", example = "Luke")
    private String givenName2;

    @Schema(description = "Last Name", example = "Smith", required = true)
    private String lastName;

    @Schema(description = "Requested Name", example = "Dave")
    private String requestedName;

    @Schema(description = "Date of Birth", example = "1970-05-01", required = true)
    private LocalDate dateOfBirth;

    @Schema(description = "Gender", example = "Male", required = true)
    private String gender;

    @Schema(description = "Indicated that is English speaking", example = "true", required = true)
    private boolean englishSpeaking;

    @Schema(description = "Level 1 Location Unit Code", example = "A")
    private String unitCode1;

    @Schema(description = "Level 2 Location Unit Code", example = "2")
    private String unitCode2;

    @Schema(description = "Level 3 Location Unit Code", example = "003")
    private String unitCode3;

    @Schema(description = "Date Prisoner booking was initial made", example = "2017-05-01")
    private LocalDate bookingBeginDate;

    @Schema(description = "Date of admission into this prison", example = "2019-06-01")
    private LocalDate admissionDate;

    @Schema(description = "Confirmed, actual, approved, provisional or calculated release date for offender, according to offender release date algorithm." +
            "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>", example = "2021-04-12")
    private LocalDate releaseDate;

    @Schema(description = "Category of this prisoner", example = "C")
    private String categoryCode;

    @Schema(description = "Status of prisoner in community", required = true, example = "ACTIVE IN", allowableValues = "ACTIVE IN,ACTIVE OUT")
    private String communityStatus;

    @Schema(description = "Legal Status", example = "REMAND")
    private LegalStatus legalStatus;

    @Schema(description = "Establishment Name for prisoner", example = "Moorland", required = true)
    private String establishmentName;

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
