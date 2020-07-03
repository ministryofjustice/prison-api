package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

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
