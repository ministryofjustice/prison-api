package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Schema(description = "Account Transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "type", "description", "amount", "date"})
public class AccountTransaction {

    @Schema(description = "Transaction ID", required = true, example = "204564839-3")
    private String id;

    @Schema(description = "The type of transaction", required = true)
    private CodeDescription type;

    @Schema(description = "Transaction description", example = "Transfer In Regular from caseload PVR", required = true)
    private String description;

    @Schema(description = "Amount in pence", example = "12345", required = true)
    private Long amount;

    @Schema(description = "Date of the transaction", example = "2016-10-21", required = true)
    private LocalDate date;
}
