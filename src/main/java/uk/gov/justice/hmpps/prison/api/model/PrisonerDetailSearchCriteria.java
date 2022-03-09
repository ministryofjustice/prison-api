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
import lombok.experimental.Wither;

import javax.validation.constraints.Max;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Prisoner Search Criteria")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Wither
@ToString
public class PrisonerDetailSearchCriteria {

    @Schema(description = "List of offender Numbers (NOMS ID)")
    private List<String> offenderNos;
    @Schema(description = "The first name of the offender.", example = "John")
    private String firstName;
    @Schema(description = "Offender's gender code (F - Female, M - Male, NK - Not Known or NS - Not Specified).", example = "F", allowableValues = "M,F,NK,NS,ALL")
    private String gender;
    @Schema(description = "The middle name(s) of the offender.", example = "James")
    private String middleNames;
    @Schema(description = "The last name of the offender.", example = "Smith")
    private String lastName;
    @Schema(description = "Offender's location filter (IN, OUT or ALL) - defaults to ALL.", example = "ALL", allowableValues = "IN,OUT,ALL")
    private String location;
    @Schema(description = "The offender's PNC (Police National Computer) number.", example = "123/1231")
    private String pncNumber;
    @Schema(description = "The offender's CRO (Criminal Records Office) number.", example = "12312312")
    private String croNumber;
    @Schema(description = "The offender's date of birth. Cannot be used in conjunction with <i>dobFrom</i> or <i>dobTo</i>. Must be specified using YYYY-MM-DD format.", example = "2001-01-15")
    private LocalDate dob;
    @Schema(description = "Start date for offender date of birth search. If <i>dobTo</i> is not specified, an implicit <i>dobTo</i> value of <i>dobFrom</i> + 10 years will be applied. If <i>dobTo</i> is specified, it will be adjusted, if necessary, to ensure it is not more than 10 years after <i>dobFrom</i>. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.", example = "1999-05-25")
    private LocalDate dobFrom;
    @Schema(description = "End date for offender date of birth search. If <i>dobFrom</i> is not specified, an implicit <i>dobFrom</i> value of <i>dobTo</i> - 10 years will be applied. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.", example = "2005-12-31")
    private LocalDate dobTo;
    @Schema(description = "Max Date Range, applied to <i>dobFrom</i> or <i>dobTo</i>, default is 10, max allowed is 10", example = "10")
    @Max(value = 10)
    @Builder.Default
    private int maxYearsRange = 10;
    @Schema(description = "If true the result set should include a row for every matched alias.  If the request includes some combination of firstName, lastName and dateOfBirth then this will be a subset of the OFFENDERS records for one or more offenders. Otherwise it will be every OFFENDERS record for each match on the other search criteria. Default is false.")
    private boolean includeAliases;
    @Schema(description = "If <i>true</i>, the search will use partial, start-of-name matching of offender names (where provided). For example, if <i>lastName</i> criteria of 'AD' is specified, this will match an offender whose last name is 'ADAMS' but not an offender whose last name is 'HADAD'. This will typically increase the number of matching offenders found. This parameter can be used with any other search processing parameter (e.g. <i>prioritisedMatch</i> or <i>anyMatch</i>).", example = "false")
    private boolean partialNameMatch;
    @Schema(description = "If <i>true</i>, offenders that match any of the specified criteria will be returned. The default search behaviour is to only return offenders that match <i>all</i> of the specified criteria. If the <i>prioritisedMatch</i> parameter is also set <i>true</i>, this parameter will only impact the behaviour of searching using offender name and date of birth criteria.", example = "false")
    private boolean anyMatch;
    @Schema(description = "If <i>true</i>, search criteria prioritisation is used and searching/matching will stop as soon as one or more matching offenders are found. The criteria priority is:<br/><br/>1. <i>offenderNo</i><br/> 2. <i>pncNumber</i><br/>3. <i>croNumber</i><br/>4. <i>firstName</i>, <i>lastName</i>, <i>dob</i> <br/>5. <i>dobFrom</i>, <i>dobTo</i><br/><br/>As an example of how this works, if this parameter is set <i>true</i> and an <i>offenderNo</i> is specified and an offender having this offender number is found, searching will stop and that offender will be returned immediately. If no offender matching the specified <i>offenderNo</i> is found, the search will be repeated using the next priority criteria (<i>pncNumber</i>) and so on. Note that offender name and date of birth criteria have the same priority and will be used together to search for matching offenders.", example = "false")
    private boolean prioritisedMatch;
}
