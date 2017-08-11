package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "bookingId",
        "bookingNo",
        "offenderNo",
        "firstName",
        "middleName",
        "lastName",
        "dateOfBirth",
        "age",
        "alertsCodes",
        "agencyId",
        "assignedLivingUnitId",
        "assignedLivingUnitDesc",
        "facialImageId",
        "assignedOfficerUserId",
        "aliases"
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffenderBookingImpl implements OffenderBooking {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @JsonProperty("bookingId")
    private BigDecimal bookingId;

    @JsonProperty("bookingNo")
    private String bookingNo;

    @JsonProperty("offenderNo")
    private String offenderNo;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("dateOfBirth")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    private Date dateOfBirth;

    @JsonProperty("age")
    private int age;

    @JsonProperty("alertsCodes")
    private List<String> alertsCodes;

    @JsonProperty("agencyId")
    private String agencyId;

    @JsonProperty("assignedLivingUnitId")
    private BigDecimal assignedLivingUnitId;

    @JsonProperty("assignedLivingUnitDesc")
    private String assignedLivingUnitDesc;

    @JsonProperty("facialImageId")
    private BigDecimal facialImageId;

    @JsonProperty("assignedOfficerUserId")
    private String assignedOfficerUserId;

    @JsonProperty("aliases")
    private List<String> aliases;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("bookingId")
    public BigDecimal getBookingId() {
        return this.bookingId;
    }

    @JsonProperty("bookingId")
    public void setBookingId(BigDecimal bookingId) {
        this.bookingId = bookingId;
    }

    @JsonProperty("bookingNo")
    public String getBookingNo() {
        return this.bookingNo;
    }

    @JsonProperty("bookingNo")
    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return this.offenderNo;
    }

    @JsonProperty("offenderNo")
    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return this.firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("middleName")
    public String getMiddleName() {
        return this.middleName;
    }

    @JsonProperty("middleName")
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return this.lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("dateOfBirth")
    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    @JsonProperty("dateOfBirth")
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @JsonProperty("age")
    public int getAge() {
        return this.age;
    }

    @JsonProperty("age")
    public void setAge(int age) {
        this.age = age;
    }

    @JsonProperty("alertsCodes")
    public List<String> getAlertsCodes() {
        return this.alertsCodes;
    }

    @JsonProperty("alertsCodes")
    public void setAlertsCodes(List<String> alertsCodes) {
        this.alertsCodes = alertsCodes;
    }

    @JsonProperty("agencyId")
    public String getAgencyId() {
        return this.agencyId;
    }

    @JsonProperty("agencyId")
    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @JsonProperty("assignedLivingUnitId")
    public BigDecimal getAssignedLivingUnitId() {
        return this.assignedLivingUnitId;
    }

    @JsonProperty("assignedLivingUnitId")
    public void setAssignedLivingUnitId(BigDecimal assignedLivingUnitId) {
        this.assignedLivingUnitId = assignedLivingUnitId;
    }

    @JsonProperty("assignedLivingUnitDesc")
    public String getAssignedLivingUnitDesc() {
        return this.assignedLivingUnitDesc;
    }

    @JsonProperty("assignedLivingUnitDesc")
    public void setAssignedLivingUnitDesc(String assignedLivingUnitDesc) {
        this.assignedLivingUnitDesc = assignedLivingUnitDesc;
    }

    @JsonProperty("facialImageId")
    public BigDecimal getFacialImageId() {
        return this.facialImageId;
    }

    @JsonProperty("facialImageId")
    public void setFacialImageId(BigDecimal facialImageId) {
        this.facialImageId = facialImageId;
    }

    @JsonProperty("assignedOfficerUserId")
    public String getAssignedOfficerUserId() {
        return this.assignedOfficerUserId;
    }

    @JsonProperty("assignedOfficerUserId")
    public void setAssignedOfficerUserId(String assignedOfficerUserId) {
        this.assignedOfficerUserId = assignedOfficerUserId;
    }

    @JsonProperty("aliases")
    public List<String> getAliases() {
        return this.aliases;
    }

    @JsonProperty("aliases")
    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }
}
