package uk.gov.justice.hmpps.prison.api.model.digitalwarrant;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "A new offence from a digital warrant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offence {

    private String offenceCode;

    private String offenceStatue;

    private LocalDate offenceDate;

    private LocalDate offenceEndDate;

    private boolean guilty;

    private Long courtCaseId;

}
