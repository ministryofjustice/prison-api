package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ReferenceCode;
import org.springframework.data.repository.CrudRepository;

public interface ReferenceCodeRepository<T extends ReferenceCode> extends CrudRepository<T, ReferenceCode.Pk> {
}
