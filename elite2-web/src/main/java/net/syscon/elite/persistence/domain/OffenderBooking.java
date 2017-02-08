package net.syscon.elite.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="offender_bookings")
@SuppressWarnings("serial")
public class OffenderBooking {

	@Id
	@Column(name="offender_book_id")
	private Long bookingId;

	@Column(name="booking_no")
	private String bookingNumber;

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public String getBookingNumber() {
		return bookingNumber;
	}

	public void setBookingNumber(String bookingNumber) {
		this.bookingNumber = bookingNumber;
	}
}

