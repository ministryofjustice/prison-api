package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Legal Case")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"case_info_number", "case_active", "case_started", "court", "legal_case_type", "charges"})
@ToString
@Builder
public class LegalCase {

    @JsonIgnore
    @Schema(description = "Case Id", example = "2323133")
    private Long caseId;

    @Schema(description = "Case Information Number", example = "1254232")
    @JsonProperty("case_info_number")
    private String caseInfoNumber;

    @Schema(description = "Case Active", example = "true")
    @JsonProperty("case_active")
    private boolean caseActive;

    @Schema(description = "Case Started Date", example = "2019-01-17")
    @JsonProperty("case_started")
    private LocalDate beginDate;

    @Schema(description = "Court", example = "{ \"code\": \"ABDRCT\", \"desc\": \"Aberdare County Court\" }")
    private CodeDescription court;

    @Schema(description = "Legal Case Type", example = "{ \"code\": \"A\", \"desc\": \"Adult\" }")
    @JsonProperty("legal_case_type")
    private CodeDescription caseType;

    @Schema(description = "Charges")
    List<Charge> charges;

}
