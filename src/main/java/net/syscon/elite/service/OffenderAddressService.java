package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderAddress;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface OffenderAddressService {

    List<OffenderAddress> getAddressesByOffenderNo(@NotNull String offenderNo);
}
