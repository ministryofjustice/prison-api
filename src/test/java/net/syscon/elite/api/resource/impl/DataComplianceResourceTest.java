package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PendingDeletionRequest;
import org.junit.Test;

import java.time.LocalDateTime;

import static net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken.ELITE2_API_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.ACCEPTED_202;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.springframework.http.HttpMethod.POST;

public class DataComplianceResourceTest extends ResourceTest {

    @Test
    public void requestOffenderPendingDeletions() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER),
                PendingDeletionRequest.builder()
                        .dueForDeletionWindowEnd(LocalDateTime.now())
                        .build());

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(ACCEPTED_202);
    }

    @Test
    public void requestOffenderPendingDeletionsExpectsBodyNotNull() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER), null);

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(BAD_REQUEST_400);
    }

    @Test
    public void requestOffenderPendingDeletionsExpectsDeletionWindowEnd() {

        final var requestEntity = createHttpEntity(authTokenHelper.getToken(ELITE2_API_USER),
                PendingDeletionRequest.builder().build());

        final var response = testRestTemplate.exchange("/api/data-compliance/offenders/pending-deletions", POST, requestEntity, Void.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(BAD_REQUEST_400);
    }
}
