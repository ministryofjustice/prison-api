package net.syscon.elite.v2.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Offender Bookings")
@JsonDeserialize(
        as = OffenderBookingImpl.class
)
public interface OffenderBooking {
    Map<String, Object> getAdditionalProperties();

    @ApiModelProperty(hidden = true)
    void setAdditionalProperties(Map<String, Object> additionalProperties);

    BigDecimal getBookingId();

    @ApiModelProperty(value = "Unique identifier offender booking.", required = true, position = 1)
    void setBookingId(BigDecimal bookingId);

    String getBookingNo();

    @ApiModelProperty(value = "A Booking Number for this offender", position = 2)
    void setBookingNo(String bookingNo);

    String getOffenderNo();

    @ApiModelProperty(value = "Unique business identifier for an offender", required = true, position = 3)
    void setOffenderNo(String offenderNo);

    String getFirstName();

    @ApiModelProperty(value = "Offender's first name used in this booking", required = true, position = 4)
    void setFirstName(String firstName);

    String getMiddleName();

    @ApiModelProperty(value = "Offender's middle name used in this booking", position = 5)
    void setMiddleName(String middleName);

    String getLastName();

    @ApiModelProperty(value = "Offender's last name used in this booking.", required = true, position = 6)
    void setLastName(String lastName);

    Date getDateOfBirth();

    @ApiModelProperty(value = "Offender's date of birth used in this booking", required = true, position = 7)
    void setDateOfBirth(Date dateOfBirth);

    int getAge();

    @ApiModelProperty(value = "Offender's age based on their date of birth", position = 8)
    void setAge(int age);

    List<String> getAlertsCodes();

    @ApiModelProperty(value = "List of active alert codes for this offender booking", position = 9)
    void setAlertsCodes(List<String> alertsCodes);

    String getAgencyId();

    @ApiModelProperty(value = "Related agency ID for this offender booking", position = 10)
    void setAgencyId(String agencyId);

    BigDecimal getAssignedLivingUnitId();

    @ApiModelProperty(value = "Offenders assigned living unit for this booking", position = 11)
    void setAssignedLivingUnitId(BigDecimal assignedLivingUnitId);

    String getAssignedLivingUnitDesc();

    @ApiModelProperty(value = "A textual description of the offenders living unit", position = 12)
    void setAssignedLivingUnitDesc(String assignedLivingUnitDesc);

    BigDecimal getFacialImageId();

    @ApiModelProperty(value = "The ID if the active offender image", position = 13)
    void setFacialImageId(BigDecimal facialImageId);

    String getAssignedOfficerUserId();

    @ApiModelProperty(value = "The ID of the staff member assigned to this offender", position = 14)
    void setAssignedOfficerUserId(String assignedOfficerUserId);

    List<String> getAliases();

    @ApiModelProperty(value = "List of aliases for the offender, displayed lastname, firstname", required = true, position = 15)
    void setAliases(List<String> aliases);
}
