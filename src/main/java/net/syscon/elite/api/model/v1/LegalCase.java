package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"case_info_number", "case_active", "case_started", "court", "legal_case_type", "charges"})
@ToString
public class LegalCase {

    @JsonProperty("charges")
    List<Charge> charges;
    @JsonIgnore
    private Long caseId;
    @JsonProperty("case_info_number")
    private String caseInfoNumber;
    @JsonProperty("case_started")
    private LocalDate beginDate;
    @JsonProperty("case_active")
    private boolean caseActive;
    @JsonProperty("court")
    private CodeDescription court;
    @JsonProperty("legal_case_type")
    private CodeDescription caseType;


}
