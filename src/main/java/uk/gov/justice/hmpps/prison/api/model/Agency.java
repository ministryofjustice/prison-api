package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription;

import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
public class Agency {
    @Schema(requiredMode = REQUIRED, description = "Agency identifier.", example = "MDI")
    private String agencyId;

    @Schema(requiredMode = REQUIRED, description = "Agency description.", example = "Moorland (HMP & YOI)")
    private String description;

    @Schema(description = "Long description of the agency", example = "Moorland (HMP & YOI)")
    private String longDescription;

    @Schema(requiredMode = REQUIRED, description = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = {"CRC","POLSTN","INST","COMM","APPR","CRT","POLICE","IMDC","TRN","OUT","YOT","SCH","STC","HOST","AIRPORT","HSHOSP","HOSPITAL","PECS","PAR","PNP","PSY"})
    private String agencyType;

    @Schema(requiredMode = REQUIRED, description = "Indicates the Agency is active", example = "true")
    @Default
    private boolean active = true;

    @Schema(description = "Court Type.  Reference domain is JURISDICTION", example = "CC", allowableValues = {"CACD","CB","CC","CO","DCM","GCM","IMM","MC","OTHER","YC"}, nullable = true)
    private String courtType;

    @Schema(description = "Court Type description.  Reference domain is JURISDICTION. Not always present for all end points", example = "Crown Court", nullable = true)
    private String courtTypeDescription;

    @Schema(description = "Date agency became inactive", example = "2012-01-12")
    private LocalDate deactivationDate;

    @Schema(description = "List of addresses associated with agency")
    private List<AddressDto> addresses;

    @Schema(description = "List of phones associated with agency")
    private List<Telephone> phones;

    @Schema(description = "List of emails associated with agency")
    private List<Email> emails;

    @Schema(description = "Area of this agency")
    private CodeDescription area;

    @Schema(description = "Region of this agency")
    private CodeDescription region;

    @Schema(description = "Geographical region for this agency")
    private CodeDescription geographicalRegion;

    public Agency(String agencyId, String description, String longDescription, String agencyType, boolean active, String courtType, String courtTypeDescription, LocalDate deactivationDate, List<AddressDto> addresses, List<Telephone> phones, List<Email> emails, CodeDescription area, CodeDescription region, CodeDescription geographicalRegion) {
        this.agencyId = agencyId;
        this.description = description;
        this.longDescription = longDescription;
        this.agencyType = agencyType;
        this.active = active;
        this.courtType = courtType;
        this.courtTypeDescription = courtTypeDescription;
        this.deactivationDate = deactivationDate;
        this.addresses = addresses;
        this.phones = phones;
        this.emails = emails;
        this.area = area;
        this.region = region;
        this.geographicalRegion = geographicalRegion;
    }

    public Agency() {
    }
}
