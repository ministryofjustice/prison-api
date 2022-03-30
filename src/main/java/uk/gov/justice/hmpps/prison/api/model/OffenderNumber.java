package uk.gov.justice.hmpps.prison.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(description = "The NOMS Offender Number")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class OffenderNumber {

    @Schema(description = "offenderNumber")
    private String offenderNumber;

    public OffenderNumber(String offenderNumber) {
        this.offenderNumber = offenderNumber;
    }

    public OffenderNumber() {
    }
}
