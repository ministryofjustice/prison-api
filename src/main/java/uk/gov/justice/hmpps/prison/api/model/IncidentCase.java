package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.SortedSet;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Incident Case")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class IncidentCase {

    @Schema(requiredMode = REQUIRED, description = "Incident Case ID", example = "2131231")
    @NotNull
    private Long incidentCaseId;

    @Schema(requiredMode = REQUIRED, description = "Title of the case", example = "Assault on staff member")
    @NotNull
    private String incidentTitle;

    @Schema(requiredMode = REQUIRED, description = "Type of incident", example = "ASSAULT", allowableValues = {"MISC","ASSAULT","FINDS1","DISORDER","KEY_LOCK","ROOF_CLIMB","DEATH_NI","REL_ERROR","FINDS","FIRE","DAMAGE","FOOD_REF","BOMB","ATT_ESC_E","ESCAPE_ESC","DRONE","TRF3","ATT_ESCAPE","BREACH","ESCAPE_EST","FIND","TRF2","FIND1","BARRICADE","HOSTAGE","SELF_HARM","DRUGS","TOOL_LOSS","RADIO_COMP","FIREARM_ETC","CON_INDISC","KEY_LOCKNEW","CLOSE_DOWN","DEATH","ABSCOND","TRF","MOBILES"})
    @NotNull
    private String incidentType;

    @Schema(description = "Details about the case", example = "There was a big fight")
    private String incidentDetails;

    @Schema(requiredMode = REQUIRED, description = "Date the incident took place", example = "2018-02-10")
    @NotNull
    private LocalDate incidentDate;

    @Schema(requiredMode = REQUIRED, description = "Time when incident occurred", example = "2018-02-10T16:35:20")
    @NotNull
    private LocalDateTime incidentTime;

    @Schema(requiredMode = REQUIRED, description = "Staff ID who created report", example = "2131231")
    @NotNull
    private Long reportedStaffId;

    @Schema(requiredMode = REQUIRED, description = "Date when incident reported", example = "2018-02-11")
    @NotNull
    private LocalDate reportDate;

    @Schema(requiredMode = REQUIRED, description = "Time incident reported", example = "2018-02-11T08:00:00")
    @NotNull
    private LocalDateTime reportTime;

    @Schema(requiredMode = REQUIRED, example = "CLOSE",
            allowableValues = {"CLOSE","DUP","AWAN","INAN","INREQ","INAME","PIU","IUP"},
            description = "Current Status of Incident.  Note:\n" +
                    "AWAN = Awaiting Analysis\n" +
                    "INAN = In Analysis\n" +
                    "INREQ = Information Required\n" +
                    "INAME =Information Amended\n" +
                    "CLOSE = Closed\n" +
                    "PIU = Post Incident Update\n" +
                    "IUP = Incident Updated\n" +
                    "DUP = Duplicate (Created In Error)")
    @NotNull
    private String incidentStatus;

    @Schema(description = "Agency where incident happened", example = "MDI")
    private String agencyId;

    @Schema(description = "Is the response completed?", example = "true")
    private Boolean responseLockedFlag;

    @Schema(description = "Question And Answer Responses")
    private SortedSet<IncidentResponse> responses;

    @Schema(description = "Parties Involved in case")
    private SortedSet<IncidentParty> parties;
}


