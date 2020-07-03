package net.syscon.prison.repository;

import net.syscon.prison.api.model.StaffDetail;
import net.syscon.prison.api.model.StaffLocationRole;
import net.syscon.prison.api.model.StaffRole;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.PageRequest;

import java.util.List;
import java.util.Optional;

public interface StaffRepository {

    Optional<StaffDetail> findByStaffId(Long staffId);

    List<String> findEmailAddressesForStaffId(Long staffId);

    Optional<StaffDetail> findStaffByPersonnelIdentifier(String idType, String id);

    Page<StaffLocationRole> findStaffByAgencyPositionRole(String agencyId, String position, String role, String nameFilter, Long staffId, Boolean activeOnly, PageRequest pageRequest);

    Page<StaffLocationRole> findStaffByAgencyRole(String agencyId, String role, String nameFilter, Long staffId, Boolean activeOnly, PageRequest pageRequest);

    List<StaffRole> getAllRolesForAgency(Long staffId, String agencyId);
}
