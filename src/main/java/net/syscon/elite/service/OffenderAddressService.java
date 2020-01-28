package net.syscon.elite.service;

import lombok.RequiredArgsConstructor;
import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.repository.OffenderAddressRepository;
import net.syscon.elite.security.VerifyOffenderAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffenderAddressService {

    private final OffenderAddressRepository repository;

    @VerifyOffenderAccess
    public List<OffenderAddress> getAddressesByOffenderNo(@NotNull String offenderNo) {
        return repository.getAddresses(offenderNo);
    }
}
