package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Offender Employment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Employment {

    @NotNull
    @Schema(description = "Offender booking id.", example = "14", required = true)
    private Long bookingId;

    @NotNull
    @Schema(description = "Start date of employment", example = "2018-02-11", required = true)
    private LocalDate startDate;

    @Schema(description = "End date of employment", example = "2018-05-11")
    private LocalDate endDate;

    @Schema(description = "The employment post type condition", example = "Full Time")
    private String postType;

    @Schema(description = "The name of the employer", example = "Greggs")
    private String employerName;

    @Schema(description = "The name of the supervisor in the employment", example = "John Smith")
    private String supervisorName;

    @Schema(description = "Position held on job", example = "Supervisor")
    private String position;

    @Schema(description = "The reason for leaving job", example = "End of contract")
    private String terminationReason;

    @Schema(description = "Amount the offender was earning", example = "10.0")
    private BigDecimal wage;

    @Schema(description = "The frequency of wage payments", example = "Hourly")
    private String wagePeriod;

    @Schema(description = "The occupation name of the offender", example = "builder")
    private String occupation;

    @Schema(description = "A comment about the employment", example = "The employment is going well")
    private String comment;

    @Schema(description = "The employment schedule", example = "Fortnightly")
    private String schedule;

    @Schema(description = "The hours worked per week", example = "32")
    private Integer hoursWeek;

    @NotNull
    @Schema(description = "Whether the employer is aware of the offender's charges", example = "true", required = true)
    private Boolean isEmployerAware;

    @NotNull
    @Schema(description = "Whether the employer can be contacted or not", example = "false", required = true)
    private Boolean isEmployerContactable;

    @NotNull
    @Builder.Default
    @Schema(description = "A list of addresses associated with the employment", required = true)
    private List<AddressDto> addresses = new ArrayList<>();
}
