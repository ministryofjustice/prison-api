package net.syscon.elite.persistence.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="offenders")
@SuppressWarnings("serial")
public class Offender {

	@Id
	@Column(name="offender_id")
	private Long offenderId;

	@Column(name="offender_id_display")
	String offenderIdDisplay;

	@Column(name="first_name")
	private String firstName;

	@Column(name="middle_name")
	private String middleName;

	@Column(name="lastName")
	private String lastName;

	@Column(name="name_type")
	private String nameType;

	@Column(name="birth_date")
	@Temporal(TemporalType.DATE)
	private Date birthDate;


	public Long getOffenderId() {
		return offenderId;
	}

	public void setOffenderId(Long offenderId) {
		this.offenderId = offenderId;
	}

	public String getOffenderIdDisplay() {
		return offenderIdDisplay;
	}

	public void setOffenderIdDisplay(String offenderIdDisplay) {
		this.offenderIdDisplay = offenderIdDisplay;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNameType() {
		return nameType;
	}

	public void setNameType(String nameType) {
		this.nameType = nameType;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
}
