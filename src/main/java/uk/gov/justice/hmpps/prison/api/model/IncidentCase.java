package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.SortedSet;

@ApiModel(description = "Incident Case")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class IncidentCase {

    @ApiModelProperty(required = true, value = "Incident Case ID", example = "2131231")
    @NotNull
    private Long incidentCaseId;

    @ApiModelProperty(required = true, value = "Title of the case", example = "Assault on staff member", position = 1)
    @NotNull
    private String incidentTitle;

    @ApiModelProperty(required = true, value = "Type of incident", example = "ASSAULT", position = 2, allowableValues = "MISC,ASSAULT,FINDS1,DISORDER,KEY_LOCK,ROOF_CLIMB,DEATH_NI,REL_ERROR,FINDS,FIRE,DAMAGE,FOOD_REF,BOMB,ATT_ESC_E,ESCAPE_ESC,DRONE,TRF3,ATT_ESCAPE,BREACH,ESCAPE_EST,FIND,TRF2,FIND1,BARRICADE,HOSTAGE,SELF_HARM,DRUGS,TOOL_LOSS,RADIO_COMP,FIREARM_ETC,CON_INDISC,KEY_LOCKNEW,CLOSE_DOWN,DEATH,ABSCOND,TRF,MOBILES")
    @NotNull
    private String incidentType;

    @ApiModelProperty(value = "Details about the case", example = "There was a big fight", position = 3)
    private String incidentDetails;

    @ApiModelProperty(required = true, value = "Date the incident took place", example = "2018-02-10", position = 4)
    @NotNull
    private LocalDate incidentDate;

    @ApiModelProperty(required = true, value = "Time when incident occurred", example = "2018-02-10T16:35:20", position = 5)
    @NotNull
    private LocalDateTime incidentTime;

    @ApiModelProperty(required = true, value = "Staff ID who created report", example = "2131231", position = 6)
    @NotNull
    private Long reportedStaffId;

    @ApiModelProperty(required = true, value = "Date when incident reported", example = "2018-02-11", position = 7)
    @NotNull
    private LocalDate reportDate;

    @ApiModelProperty(required = true, value = "Time incident reported", example = "2018-02-11T08:00:00", position = 8)
    @NotNull
    private LocalDateTime reportTime;

    @ApiModelProperty(required = true, value = "Current Status of Incident", example = "CLOSE", position = 9,
            allowableValues = "CLOSE,DUP,AWAN,INAN,INREQ,INAME,PIU,IUP",
            notes = "AWAN = Awaiting Analysis\n" +
                    "INAN = In Analysis\n" +
                    "INREQ = Information Required\n" +
                    "INAME =Information Amended\n" +
                    "CLOSE = Closed\n" +
                    "PIU = Post Incident Update\n" +
                    "IUP = Incident Updated\n" +
                    "DUP = Duplicate (Created In Error)")
    @NotNull
    private String incidentStatus;

    @ApiModelProperty(value = "Agency where incident happened", example = "MDI", position = 10)
    private String agencyId;

    @ApiModelProperty(value = "Is the response completed?", example = "true", position = 11)
    private Boolean responseLockedFlag;

    @ApiModelProperty(value = "Question And Answer Responses", position = 12)
    private SortedSet<IncidentResponse> responses;

    @ApiModelProperty(value = "Parties Involved in case", position = 13)
    private SortedSet<IncidentParty> parties;
}


