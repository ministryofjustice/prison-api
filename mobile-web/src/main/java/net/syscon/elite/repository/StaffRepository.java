package net.syscon.elite.repository;

import net.syscon.elite.api.model.StaffDetail;

import java.util.Optional;

public interface StaffRepository {
    Optional<StaffDetail> findByStaffId(Long staffId);
}
