package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BookingNumberBlock;

public interface BookingNumberBlockRepository extends CrudRepository<BookingNumberBlock, Integer> {
    
}
