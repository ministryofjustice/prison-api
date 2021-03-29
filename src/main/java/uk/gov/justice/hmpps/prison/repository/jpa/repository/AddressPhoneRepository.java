package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone;

import java.util.List;

public interface AddressPhoneRepository extends CrudRepository<AddressPhone, Long> {

    @Query("Select pa from AddressPhone pa where pa.address.id = :addressId")
    List<AddressPhone> findAllByAddressId(Long addressId);

}
