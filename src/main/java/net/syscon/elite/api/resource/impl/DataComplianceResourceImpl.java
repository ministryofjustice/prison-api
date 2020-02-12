package net.syscon.elite.api.resource.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.DataComplianceResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${api.base.path}/data-compliance")
public class DataComplianceResourceImpl implements DataComplianceResource {

    @Override
    public ResponseEntity<Void> requestOffenderPendingDeletions(final PendingDeletionRequest request) {

        log.warn("Pending deletions request is not yet implemented, ignoring request: {}", request);

        return ResponseEntity.accepted().build();
    }
}
