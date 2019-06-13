package net.syscon.elite.service.v1;

import net.syscon.elite.api.model.v1.Location;
import net.syscon.elite.repository.v1.NomisApiV1Repository;
import org.springframework.stereotype.Service;

@Service
public class NomisApiV1Service {

    private final NomisApiV1Repository dao;

    public NomisApiV1Service(NomisApiV1Repository dao) {
        this.dao = dao;
    }

    public Location getLatestBookingLocation(String nomsId) {
        return dao.getLatestBookingLocation(nomsId).orElse(null);
    }
}
