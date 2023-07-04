package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Account Transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "type", "description", "amount", "date"})
public class AccountTransaction {

    @Schema(description = "Transaction ID", requiredMode = REQUIRED, example = "204564839-3")
    private String id;

    @Schema(description = "The type of transaction", requiredMode = REQUIRED)
    private CodeDescription type;

    @Schema(description = "Transaction description", example = "Transfer In Regular from caseload PVR", requiredMode = REQUIRED)
    private String description;

    @Schema(description = "Amount in pence", example = "12345", requiredMode = REQUIRED)
    private Long amount;

    @Schema(description = "Date of the transaction", example = "2016-10-21", requiredMode = REQUIRED)
    private LocalDate date;
}
