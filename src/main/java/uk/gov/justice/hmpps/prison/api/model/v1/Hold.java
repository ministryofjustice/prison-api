package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Schema(description = "Hold Response")
@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonInclude(Include.NON_NULL)
public class Hold {

    @Schema(description = "Hold Number", name = "hold_number", example = "6185835")
    @JsonProperty("hold_number")
    private Long holdNumber;

    @Schema(description = "Client unique reference", name = "client_unique_ref", example = "jerkincrocus")
    @JsonProperty("client_unique_ref")
    private String clientUniqueRef;

    @Schema(description = "Reference number", name = "reference_no", example = "TEST0075")
    @JsonProperty("reference_no")
    private String referenceNo;

    @Schema(description = "Description", name = "description", example = "Hold via API")
    private String description;

    @Schema(description = "Entry date", name = "entry_date", example = "2017-06-23")
    @JsonProperty("entry_date")
    private LocalDate entryDate;

    @Schema(description = "Amount in pence", name = "amount", example = "150")
    private Long amount;

    @Schema(description = "Hold until date", name = "hold_until_date", example = "2017-07-07")
    @JsonProperty("hold_until_date")
    private LocalDate holdUntilDate;

}
