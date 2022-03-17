package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference Code
 **/
@Schema(description = "Reference Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonPropertyOrder({"domain", "code", "description", "parentDomain", "parentCode", "activeFlag", "listSeq", "systemDataFlag", "expiredDate", "subCodes"})
@Data
@ToString
public class ReferenceCode extends ReferenceCodeInfo {

    @Schema(required = true, description = "Reference data item domain.", example = "TASK_TYPE")
    @NotBlank
    @Size(max = 12)
    private String domain;

    @Schema(required = true, description = "Reference data item code.", example = "MISC")
    @NotBlank
    @Size(max = 12)
    private String code;

    @Schema(description = "List of subordinate reference data items associated with this reference data item. Not returned by default")
    @Builder.Default
    private List<ReferenceCode> subCodes = new ArrayList<>();

    public ReferenceCode(@NotBlank @Size(max = 12) String domain, @NotBlank @Size(max = 12) String code, List<ReferenceCode> subCodes) {
        this.domain = domain;
        this.code = code;
        this.subCodes = subCodes;
    }

    public ReferenceCode(@NotBlank @Size(max = 12) String domain, @NotBlank @Size(max = 12) String code, List<ReferenceCode> subCodes, @NotBlank @Size(max = 40) String description, @Size(max = 12) String parentDomain, @Size(max = 12) String parentCode, @Size(max = 1) @Pattern(regexp = "[N|Y]") String activeFlag, @Max(value = 999999) Integer listSeq, @Size(max = 1) @Pattern(regexp = "[N|Y]") String systemDataFlag, LocalDate expiredDate) {
        super(description, parentDomain, parentCode, activeFlag, listSeq, systemDataFlag, expiredDate);
        this.domain = domain;
        this.code = code;
        this.subCodes = subCodes;
    }

    public ReferenceCode() {
    }
}
