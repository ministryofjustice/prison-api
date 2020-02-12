package net.syscon.elite.repository.jpa.repository

import net.syscon.elite.repository.jpa.model.OffenderBooking
import org.springframework.data.repository.CrudRepository

interface OffenderBookingRepository : CrudRepository<OffenderBooking, Long>
