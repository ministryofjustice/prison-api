package uk.gov.justice.hmpps.prison.service;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.LocationSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderCell;
import uk.gov.justice.hmpps.prison.api.model.OffenderCellAttribute;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreateAgency;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateAddress;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateAgency;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdatePhone;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AddressPhoneRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter;
import uk.gov.justice.hmpps.prison.service.support.AlphaNumericComparator;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ACTIVE_ONLY;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

/**
 * Agency API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class AgencyService {

    private static final Comparator<LocationSummary> LOCATION_DESCRIPTION_COMPARATOR = Comparator.comparing(
        LocationSummary::getUserDescription,
        new AlphaNumericComparator());

    private final HmppsAuthenticationHolder hmppsAuthenticationHolder;
    private final AgencyRepository agencyRepository;
    private final AgencyLocationRepository agencyLocationRepository;
    private final ReferenceDomainService referenceDomainService;
    private final ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeReferenceCodeRepository;
    private final ReferenceCodeRepository<CourtType> courtTypeReferenceCodeRepository;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final AddressPhoneRepository addressPhoneRepository;
    private final AgencyAddressRepository agencyAddressRepository;

    private final ReferenceCodeRepository<AddressType> addressTypeReferenceCodeRepository;
    private final ReferenceCodeRepository<City> cityReferenceCodeRepository;
    private final ReferenceCodeRepository<County> countyReferenceCodeRepository;
    private final ReferenceCodeRepository<Country> countryReferenceCodeRepository;

    public Agency getAgency(final String agencyId, @NotNull final StatusFilter filter, final String agencyType, final boolean withAddresses, final boolean skipFormatLocation, boolean withAreas) {
        final var criteria = AgencyLocationFilter.builder()
            .id(agencyId)
            .type(agencyType)
            .active(filter.getActive())
            .build();

        return agencyLocationRepository.findAll(criteria)
            .stream()
            .findFirst()
            .map(agency -> translate(withAddresses, agency, skipFormatLocation, withAreas)).orElseThrow(EntityNotFoundException.withId(agencyId));
    }

    private Agency translate(final boolean withAddresses, final AgencyLocation agency, final boolean skipFormatLocation, boolean withAreas) {
        if (withAddresses) {
            return AgencyTransformer.transformWithAddresses(agency, skipFormatLocation);
        }
        return AgencyTransformer.transform(agency, skipFormatLocation, withAreas);
    }


    @Transactional
    public Agency updateAgency(final String agencyId, final RequestToUpdateAgency agencyToUpdate) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyLocationType = agencyLocationTypeReferenceCodeRepository.findById(new uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk(AgencyLocationType.AGY_LOC_TYPE, agencyToUpdate.getAgencyType())).orElseThrow(BadRequestException.withMessage(format("Agency Type [%s] not found", agencyToUpdate.getAgencyType())));

        if (agencyLocationType.isCourt() && StringUtils.isBlank(agencyToUpdate.getCourtType())) {
            throw new EntityNotFoundException("Courts require a court type");
        }

        final var courtType = agencyToUpdate.getCourtType() != null ? courtTypeReferenceCodeRepository.findById(new uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk(CourtType.JURISDICTION, agencyToUpdate.getCourtType())).orElseThrow(BadRequestException.withMessage(format("Court Type [%s] not found", agencyToUpdate.getCourtType()))) : null;
        return AgencyTransformer.transform(AgencyTransformer.update(agency, agencyToUpdate, agencyLocationType, courtType), true, false);
    }

    @Transactional
    public Agency createAgency(final RequestToCreateAgency agencyToCreate) {
        agencyLocationRepository.findById(agencyToCreate.getAgencyId())
            .ifPresent(p -> {
                throw new EntityAlreadyExistsException(format("Agency with ID %s already exists", agencyToCreate.getAgencyId()));
            });

        final var agencyLocationType = agencyLocationTypeReferenceCodeRepository.findById(new uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk(AgencyLocationType.AGY_LOC_TYPE, agencyToCreate.getAgencyType())).orElseThrow(BadRequestException.withMessage(format("Agency Type [%s] not found", agencyToCreate.getAgencyType())));

        if (agencyLocationType.isCourt() && StringUtils.isBlank(agencyToCreate.getCourtType())) {
            throw new EntityNotFoundException("Courts require a court type");
        }
        final var courtType = agencyToCreate.getCourtType() != null ? courtTypeReferenceCodeRepository.findById(new uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk(CourtType.JURISDICTION, agencyToCreate.getCourtType())).orElseThrow(BadRequestException.withMessage(format("Court Type [%s] not found", agencyToCreate.getCourtType()))) : null;

        final var agencyLocation = agencyLocationRepository.save(AgencyTransformer.build(agencyToCreate, agencyLocationType, courtType));
        return AgencyTransformer.transform(agencyLocation, true, false);
    }

    public List<Agency> getAgenciesByType(final String agencyType,
                                          final boolean activeOnly,
                                          final List<String> courtTypes,
                                          final String areaCode,
                                          final String regionCode,
                                          final String establishmentType,
                                          final boolean skipFormatLocation,
                                          final boolean withAddresses,
                                          boolean withAreas) {

        final var filter = AgencyLocationFilter.builder()
            .active(activeOnly ? true : null)
            .type(agencyType)
            .courtTypes(courtTypes)
            .area(areaCode)
            .region(regionCode)
            .establishmentType(establishmentType)
            .build();

        List<AgencyLocation> all = agencyLocationRepository.findAll(filter);
        return all
            .stream()
            .map(agency -> translate(withAddresses, agency, skipFormatLocation, withAreas))
            .sorted(Comparator.comparing(Agency::getDescription, Comparator.naturalOrder()))
            .collect(toList());
    }

    public void checkAgencyExists(final String agencyId, final StatusFilter filter) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        if (agencyRepository.findAgency(agencyId, filter, null).isEmpty()) {
            throw EntityNotFoundException.withId(agencyId);
        }
    }

    public Page<Agency> getAgencies(final long offset, final long limit) {
        return agencyRepository.getAgencies("agencyId", Order.ASC, offset, limit);
    }

    public List<Agency> findAgenciesByUsername(final String username, final boolean allowInactive) {
        if (StringUtils.isBlank(username)) return Collections.emptyList();
        final var agenciesByUsername = agencyRepository.findAgenciesByUsername(username, allowInactive);
        agenciesByUsername.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByUsername;
    }

    /**
     * Gets set of agency location ids accessible to current authenticated user. This governs access to bookings - a user
     * cannot have access to an offender unless they are in a location that the authenticated user is also associated with.
     *
     * @param allowInactive include inactive prisons
     * @return set of agency location ids accessible to current authenticated user.
     */
    public Set<String> getAgencyIds(boolean allowInactive) {
        return findAgenciesByUsername(hmppsAuthenticationHolder.getUsername(), allowInactive)
            .stream()
            .map(Agency::getAgencyId)
            .collect(Collectors.toSet());
    }

    /**
     * Verifies that current user is authorised to access specified agency. If this
     * agency location is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param agencyId the agency.
     * @param allowInactive whether to allow inactive prisons in caseload
     * @throws EntityNotFoundException if agency does not exist.
     * @throws AccessDeniedException   if current user does not have access to this agency.
     */
    public void verifyAgencyAccess(final String agencyId, boolean allowInactive) {
        Objects.requireNonNull(agencyId, "agencyId is a required parameter");

        final var agencyIds = getAgencyIds(allowInactive);
        if (HmppsAuthenticationHolder.Companion.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (agencyIds.isEmpty() || !agencyIds.contains(agencyId)) {
            checkAgencyExists(agencyId, allowInactive ? ALL: ACTIVE_ONLY);
            throw new AccessDeniedException(format("Unauthorised access to agency with id %s due to missing override role%s", agencyId, allowInactive ? "" : ", or agency inactive"));
        }
    }

    public List<Location> getAgencyLocations(final String agencyId, final String eventType, final String sortFields, final Order sortOrder) {
        // If no sort fields defined, sort in ascending order of user description then description (by default)
        final var orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        final List<String> eventTypes = StringUtils.isBlank(eventType) ? Collections.emptyList() : Collections.singletonList(eventType);
        final var rawLocations = agencyRepository.getAgencyLocations(agencyId, eventTypes, orderBy, order);

        return LocationProcessor.processLocations(rawLocations);
    }

    public List<Location> getAgencyEventLocations(final String agencyId, final String sortFields, final Order sortOrder) {
        final var orderBy = StringUtils.defaultIfBlank(sortFields, "userDescription,description");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        // Get all location usages for locations that an event could possibly be held in. (reference domain ILOC_USG )
        // Note this should be cached. Also assuming small number of values
        final var allEventLocationUsages = referenceDomainService
            .getReferenceCodesByDomain(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(), false, null, null, 0, 1000)
            .getItems().stream().map(ReferenceCode::getCode).collect(toList());

        final var rawLocations = agencyRepository.getAgencyLocations(agencyId, allEventLocationUsages, orderBy, order);

        return LocationProcessor.processLocations(rawLocations).stream().distinct().toList();
    }

    public List<LocationSummary> getAgencyEventLocationsBooked(final String agencyId, @NotNull final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        return getAgencyLocationsOnDayAndPeriod(agencyId, bookedOnDay, bookedOnPeriod);
    }

    private List<LocationSummary> getAgencyLocationsOnDayAndPeriod(final String agencyId, @NotNull final LocalDate bookedOnDay, final TimeSlot bookedOnPeriod) {
        Objects.requireNonNull(bookedOnDay, "bookedOnDay must be specified.");

        final var locations = agencyRepository.getAgencyLocationsBooked(agencyId, bookedOnDay, bookedOnPeriod);
        final var processedLocations = LocationProcessor.processLocationSummaries(locations);
        return processedLocations.stream()
            .sorted(LOCATION_DESCRIPTION_COMPARATOR)
            .toList();
    }

    public List<PrisonContactDetail> getPrisonContactDetail() {

        final var agencyLocationType = agencyLocationTypeReferenceCodeRepository.findById(AgencyLocationType.INST).orElseThrow(EntityNotFoundException.withMessage(format("Agency Location Type of %s not Found", AgencyLocationType.INST.getCode())));
        final var prisons = agencyLocationRepository.findByTypeAndActiveAndDeactivationDateIsNull(agencyLocationType, true);

        return
            removeBlankAddresses(prisons.stream()
                .map(this::getPrisonContactDetail)
                .collect(toList())
            );
    }

    private PrisonContactDetail getPrisonContactDetail(final uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation prison) {
        if (prison.getAddresses().isEmpty()) {
            return PrisonContactDetail.builder().agencyId(prison.getId())
                .description(prison.getDescription())
                .formattedDescription(LocationProcessor.formatLocation(prison.getDescription()))
                .build();
        } else {
            final var primaryAddress = prison.getAddresses().stream()
                .filter(a -> "Y".equals(a.getPrimaryFlag())).findFirst().orElse(prison.getAddresses().iterator().next());

            final var primaryCountry = primaryAddress.getCountry() != null ? primaryAddress.getCountry().getDescription() : null;
            final var primaryTown = primaryAddress.getCity() != null ? primaryAddress.getCity().getDescription() : null;
            final var primaryAddressType = primaryAddress.getAddressType() != null ? primaryAddress.getAddressType().getCode() : null;

            return PrisonContactDetail.builder().agencyId(prison.getId())
                .description(prison.getDescription())
                .formattedDescription(LocationProcessor.formatLocation(prison.getDescription()))
                .addressType(primaryAddressType)
                .phones(primaryAddress.getPhones().stream()
                    .map(phone ->
                        Telephone.builder().number(phone.getPhoneNo()).type(phone.getPhoneType()).ext(phone.getExtNo()).build()).collect(toList()))
                .locality(primaryAddress.getLocality())
                .postCode(primaryAddress.getPostalCode())
                .premise(primaryAddress.getPremise())
                .city(primaryTown)
                .country(primaryCountry)
                .addresses(AddressTransformer.translate(prison.getAddresses()))
                .build();

        }
    }

    public PrisonContactDetail getPrisonContactDetail(final String agencyId) {
        final var agencyLocationType = agencyLocationTypeReferenceCodeRepository.findById(AgencyLocationType.INST)
            .orElseThrow(EntityNotFoundException.withMessage(format("Agency Location Type of %s not Found", AgencyLocationType.INST.getCode())));
        final var prisonContactDetailList = removeBlankAddresses(List.of(getPrisonContactDetail(agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNull(agencyId, agencyLocationType, true)
            .orElseThrow(EntityNotFoundException.withMessage(format("Contact details not found for Prison %s", agencyId))))));

        if (prisonContactDetailList.isEmpty()) {
            throw EntityNotFoundException.withMessage(format("Contact details not found for Prison %s", agencyId));
        }
        return prisonContactDetailList.getFirst();

    }

    public List<Agency> getAgenciesByCaseload(final String caseload) {
        final var agenciesByCaseload = agencyRepository.findAgenciesByCaseload(caseload);
        agenciesByCaseload.forEach(a -> a.setDescription(LocationProcessor.formatLocation(a.getDescription())));
        return agenciesByCaseload;
    }

    //It is possible for invalid/empty address records to be persisted
    @VisibleForTesting
    List<PrisonContactDetail> removeBlankAddresses(final List<PrisonContactDetail> list) {
        return list.stream().filter(pcd -> !isBlankAddress(pcd)).collect(toList());
    }

    private boolean isBlankAddress(final PrisonContactDetail pcd) {
        return pcd.getPremise() == null && pcd.getCity() == null && pcd.getLocality() == null && pcd.getPostCode() == null;
    }

    public List<OffenderCell> getReceptionsWithCapacityInAgency(@NotNull final String agencyId, final String attribute) {
        final var receptions = agencyInternalLocationRepository.findWithProfilesAgencyInternalLocationsByAgencyIdAndLocationCodeAndActive(agencyId, "RECP", true);
        return receptions.stream()
            .filter(AgencyInternalLocation::isActiveReceptionWithSpace)
            .map(this::transform)
            .filter(recep -> attribute == null || recep.getAttributes().stream().anyMatch(a -> a.getCode().equals(attribute)))
            .collect(toList());
    }

    public OffenderCell getCellAttributes(@NotNull final Long locationId) {
        final var agencyInternalLocation = agencyInternalLocationRepository.findOneByLocationId(locationId);
        final var offenderCell = agencyInternalLocation.map(this::transform).orElse(null);
        if (offenderCell == null) {
            throw EntityNotFoundException.withMessage(format("No cell details found for location id %s", locationId));
        }
        return offenderCell;
    }

    public AgencyEstablishmentTypes getEstablishmentTypes(final String agencyId) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));
        return AgencyEstablishmentTypes.builder().agencyId(agencyId).establishmentTypes(
                agency.getEstablishmentTypes()
                    .stream()
                    .map(establishment -> AgencyEstablishmentType.builder()
                        .code(establishment.getEstablishmentType().getCode())
                        .description(establishment.getEstablishmentType().getDescription())
                        .build())
                    .collect(toList()))
            .build();
    }

    private OffenderCell transform(final AgencyInternalLocation cell) {
        final var attributes = cell.getProfiles()
            .stream()
            .filter(AgencyInternalLocationProfile::isAttribute)
            .map(AgencyInternalLocationProfile::getHousingAttributeReferenceCode)
            .map(referenceCode -> OffenderCellAttribute.builder()
                .code(referenceCode.getCode())
                .description(referenceCode.getDescription())
                .build())
            .collect(toList());
        return OffenderCell.builder()
            .capacity(cell.getActualCapacity())
            .noOfOccupants(cell.getCurrentOccupancy())
            .id(cell.getLocationId())
            .description(cell.getDescription())
            .userDescription(cell.getUserDescription())
            .attributes(attributes)
            .build();
    }

    @Transactional
    public AddressDto createAgencyAddress(final String agencyId, final RequestToUpdateAddress requestToUpdateAddress) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final AgencyAddress address = AgencyAddress.builder()
            .flat(requestToUpdateAddress.getFlat())
            .premise(requestToUpdateAddress.getPremise())
            .street(requestToUpdateAddress.getStreet())
            .locality(requestToUpdateAddress.getLocality())
            .postalCode(requestToUpdateAddress.getPostalCode())
            .primaryFlag(requestToUpdateAddress.isPrimary() ? "Y" : "N")
            .noFixedAddressFlag(requestToUpdateAddress.isNoFixedAddress() ? "Y" : "N")
            .startDate(requestToUpdateAddress.getStartDate())
            .endDate(requestToUpdateAddress.getEndDate())
            .commentText(requestToUpdateAddress.getComment())
            .addressType(addressTypeReferenceCodeRepository.findById(AddressType.pk(requestToUpdateAddress.getAddressType())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getAddressType())))
            .city(StringUtils.isNotBlank(requestToUpdateAddress.getTown()) ? cityReferenceCodeRepository.findById(City.pk(requestToUpdateAddress.getTown())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getTown())) : null)
            .county(StringUtils.isNotBlank(requestToUpdateAddress.getCounty()) ? countyReferenceCodeRepository.findById(County.pk(requestToUpdateAddress.getCounty())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getCounty())) : null)
            .country(StringUtils.isNotBlank(requestToUpdateAddress.getCountry()) ? countryReferenceCodeRepository.findById(Country.pk(requestToUpdateAddress.getCountry())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getCountry())) : null)
            .build();

        return AddressTransformer.translate(agencyAddressRepository.save(agency.addAddress(address)));
    }

    @Transactional
    public AddressDto updateAgencyAddress(final String agencyId, final Long addressId, final RequestToUpdateAddress requestToUpdateAddress) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyAddress = agency.getAddresses().stream()
            .filter(a -> a.getAddressId().equals(addressId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(addressId));

        agencyAddress.setFlat(requestToUpdateAddress.getFlat());
        agencyAddress.setPremise(requestToUpdateAddress.getPremise());
        agencyAddress.setStreet(requestToUpdateAddress.getStreet());
        agencyAddress.setLocality(requestToUpdateAddress.getLocality());
        agencyAddress.setPostalCode(requestToUpdateAddress.getPostalCode());
        agencyAddress.setPrimaryFlag(requestToUpdateAddress.isPrimary() ? "Y" : "N");
        agencyAddress.setNoFixedAddressFlag(requestToUpdateAddress.isNoFixedAddress() ? "Y" : "N");
        agencyAddress.setStartDate(requestToUpdateAddress.getStartDate());
        agencyAddress.setEndDate(requestToUpdateAddress.getEndDate());
        agencyAddress.setCommentText(requestToUpdateAddress.getComment());

        agencyAddress.setAddressType(addressTypeReferenceCodeRepository.findById(AddressType.pk(requestToUpdateAddress.getAddressType())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getAddressType())));
        agencyAddress.setCity(StringUtils.isNotBlank(requestToUpdateAddress.getTown()) ? cityReferenceCodeRepository.findById(City.pk(requestToUpdateAddress.getTown())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getTown())) : null);
        agencyAddress.setCounty(StringUtils.isNotBlank(requestToUpdateAddress.getCounty()) ? countyReferenceCodeRepository.findById(County.pk(requestToUpdateAddress.getCounty())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getCounty())) : null);
        agencyAddress.setCountry(StringUtils.isNotBlank(requestToUpdateAddress.getCountry()) ? countryReferenceCodeRepository.findById(Country.pk(requestToUpdateAddress.getCountry())).orElseThrow(EntityNotFoundException.withId(requestToUpdateAddress.getCountry())) : null);

        return AddressTransformer.translate(agencyAddress);
    }

    @Transactional
    public void deleteAgencyAddress(final String agencyId, final Long addressId) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyAddress = agency.getAddresses().stream()
            .filter(a -> a.getAddressId().equals(addressId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(addressId));

        agency.removeAddress(agencyAddress);
    }

    @Transactional
    public Telephone createAgencyAddressPhone(final String agencyId, final Long addressId, final RequestToUpdatePhone requestToUpdatePhone) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyAddress = agency.getAddresses().stream()
            .filter(a -> a.getAddressId().equals(addressId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(addressId));

        final var phone = AddressPhone.builder()
            .phoneNo(requestToUpdatePhone.getNumber())
            .extNo(requestToUpdatePhone.getExt())
            .phoneType(requestToUpdatePhone.getType())
            .build();

        return AddressTransformer.translate(addressPhoneRepository.save(agencyAddress.addPhone(phone)));
    }

    @Transactional
    public Telephone updateAgencyAddressPhone(final String agencyId, final Long addressId, final Long phoneId, final RequestToUpdatePhone requestToUpdatePhone) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyAddress = agency.getAddresses().stream()
            .filter(a -> a.getAddressId().equals(addressId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(addressId));

        final var addressPhone = agencyAddress.getPhones().stream()
            .filter(p -> p.getPhoneId().equals(phoneId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(phoneId));

        addressPhone.setPhoneNo(requestToUpdatePhone.getNumber());
        addressPhone.setExtNo(requestToUpdatePhone.getExt());
        addressPhone.setPhoneType(requestToUpdatePhone.getType());

        return AddressTransformer.translate(addressPhone);
    }

    @Transactional
    public void deleteAgencyAddressPhone(final String agencyId, final Long addressId, final Long phoneId) {
        final var agency = agencyLocationRepository.findById(agencyId).orElseThrow(EntityNotFoundException.withId(agencyId));

        final var agencyAddress = agency.getAddresses().stream()
            .filter(a -> a.getAddressId().equals(addressId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(addressId));

        final var addressPhone = agencyAddress.getPhones().stream()
            .filter(p -> p.getPhoneId().equals(phoneId))
            .findFirst().orElseThrow(EntityNotFoundException.withId(phoneId));

        agencyAddress.removePhone(addressPhone);
    }
}
