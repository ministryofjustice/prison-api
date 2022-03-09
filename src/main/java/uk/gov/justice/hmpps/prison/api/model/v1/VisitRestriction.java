package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Visit Restriction")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class VisitRestriction {

    @Schema(description = "Type", name = "type")
    @JsonProperty("type")
    private CodeDescription restrictionType;

    @Schema(description = "Effective Date", name = "effective_date", example = "2019-01-01")
    @JsonProperty("effective_date")
    private LocalDate effectiveDate;

    @Schema(description = "Expiry Date", name = "expiry_date", example = "2019-01-01")
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @Schema(description = "Comment Text", name = "comment_text")
    @JsonProperty("comment_text")
    private String commentText;


}
