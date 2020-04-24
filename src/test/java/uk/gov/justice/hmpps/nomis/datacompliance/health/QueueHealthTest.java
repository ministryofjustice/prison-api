package uk.gov.justice.hmpps.nomis.datacompliance.health;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Status;
import uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.DlqStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.QueueAttributes.MESSAGES_IN_FLIGHT;
import static uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.QueueAttributes.MESSAGES_ON_DLQ;
import static uk.gov.justice.hmpps.nomis.datacompliance.health.QueueHealth.QueueAttributes.MESSAGES_ON_QUEUE;

@ExtendWith(MockitoExtension.class)
class QueueHealthTest {

    private static final String SOME_QUEUE_NAME = "some queue name";
    private static final String SOME_QUEUE_URL = "some queue url";
    private static final String SOME_DLQ_NAME = "some DLQ name";
    private static final String SOME_DLQ_URL = "some DLQ url";
    private static final Integer SOME_MESSAGES_ON_QUEUE_COUNT = 123;
    private static final Integer SOME_MESSAGES_IN_FLIGHT_COUNT = 456;
    private static final Integer SOME_MESSAGES_ON_DLQ_COUNT = 789;

    @Mock
    private AmazonSQS amazonSqs, amazonSqsDlq;

    private QueueHealth queueHealth;

    @BeforeEach
    void setUp() {
        queueHealth = new QueueHealth(amazonSqs, amazonSqsDlq, SOME_QUEUE_NAME, SOME_DLQ_NAME) { };
    }

    @Test
    void queueHealthReportsHealthy() {
        mockHealthyQueue();

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails().get(MESSAGES_ON_QUEUE.healthName)).isEqualTo(SOME_MESSAGES_ON_QUEUE_COUNT.toString());
        assertThat(health.getDetails().get(MESSAGES_IN_FLIGHT.healthName)).isEqualTo(SOME_MESSAGES_IN_FLIGHT_COUNT.toString());
    }

    @Test
    void queueHealthReportsUnhealthy() {

        when(amazonSqs.getQueueUrl(SOME_QUEUE_NAME)).thenThrow(QueueDoesNotExistException.class);

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void queueHealthReportsUnhealthyWhenGetMainQueueAttributesUnavailable() {

        when(amazonSqs.getQueueUrl(anyString())).thenReturn(someGetQueueUrlResult());
        when(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenThrow(RuntimeException.class);

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void queueHealthReportsDlqDetails() {

        mockHealthyQueue();

        final var health = queueHealth.health();

        assertThat(health.getDetails().get("dlqStatus")).isEqualTo(DlqStatus.UP.description);
        assertThat(health.getDetails().get(MESSAGES_ON_DLQ.healthName)).isEqualTo(SOME_MESSAGES_ON_DLQ_COUNT.toString());
    }

    @Test
    void queueHealthReportsUnhealthyIfNoRedrivePolicy() {
        when(amazonSqs.getQueueUrl(SOME_QUEUE_NAME)).thenReturn(someGetQueueUrlResult());
        when(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(someGetQueueAttributesResultWithoutDlq());

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("dlqStatus")).isEqualTo(DlqStatus.NOT_ATTACHED.description);
    }

    @Test
    void queueHealthReportsUnhealthyIfDlqNotFound() {
        when(amazonSqs.getQueueUrl(SOME_QUEUE_NAME)).thenReturn(someGetQueueUrlResult());
        when(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(someGetQueueAttributesResultWithDlq());
        when(amazonSqsDlq.getQueueUrl(SOME_DLQ_NAME)).thenThrow(QueueDoesNotExistException.class);

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("dlqStatus")).isEqualTo(DlqStatus.NOT_FOUND.description);
    }

    @Test
    void queueHealthReportsUnhealthyIfDlqAttributesNotAvailable() {
        when(amazonSqs.getQueueUrl(SOME_QUEUE_NAME)).thenReturn(someGetQueueUrlResult());
        when(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(someGetQueueAttributesResultWithDlq());
        when(amazonSqsDlq.getQueueUrl(SOME_DLQ_NAME)).thenReturn(someGetQueueUrlResultForDlq());
        when(amazonSqsDlq.getQueueAttributes(someGetQueueAttributesRequestForDlq())).thenThrow(RuntimeException.class);

        final var health = queueHealth.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("dlqStatus")).isEqualTo(DlqStatus.NOT_AVAILABLE.description);
    }

    private void mockHealthyQueue() {
        when(amazonSqs.getQueueUrl(SOME_QUEUE_NAME)).thenReturn(someGetQueueUrlResult());
        when(amazonSqs.getQueueAttributes(someGetQueueAttributesRequest())).thenReturn(someGetQueueAttributesResultWithDlq());
        when(amazonSqsDlq.getQueueUrl(SOME_DLQ_NAME)).thenReturn(someGetQueueUrlResultForDlq());
        when(amazonSqsDlq.getQueueAttributes(someGetQueueAttributesRequestForDlq())).thenReturn(someGetQueueAttributesResultForDlq());
    }

    private GetQueueUrlResult someGetQueueUrlResult() {
        return new GetQueueUrlResult().withQueueUrl(SOME_QUEUE_URL);
    }

    private GetQueueAttributesRequest someGetQueueAttributesRequest() {
        return new GetQueueAttributesRequest(SOME_QUEUE_URL)
                .withAttributeNames(List.of(QueueAttributeName.All.toString()));
    }

    private GetQueueAttributesResult someGetQueueAttributesResultWithoutDlq() {
        return new GetQueueAttributesResult().withAttributes(Map.of(
                MESSAGES_ON_QUEUE.awsName, SOME_MESSAGES_ON_QUEUE_COUNT.toString(),
                MESSAGES_IN_FLIGHT.awsName, SOME_MESSAGES_IN_FLIGHT_COUNT.toString()));
    }

    private GetQueueAttributesResult someGetQueueAttributesResultWithDlq() {
        return new GetQueueAttributesResult().withAttributes(Map.of(
                MESSAGES_ON_QUEUE.awsName, SOME_MESSAGES_ON_QUEUE_COUNT.toString(),
                MESSAGES_IN_FLIGHT.awsName, SOME_MESSAGES_IN_FLIGHT_COUNT.toString(),
                QueueAttributeName.RedrivePolicy.toString(), "any redrive policy"));

    }

    private GetQueueAttributesRequest someGetQueueAttributesRequestForDlq() {
        return new GetQueueAttributesRequest(SOME_DLQ_URL)
                .withAttributeNames(List.of(QueueAttributeName.All.toString()));
    }

    private GetQueueUrlResult someGetQueueUrlResultForDlq() {
        return new GetQueueUrlResult().withQueueUrl(SOME_DLQ_URL);
    }

    private GetQueueAttributesResult someGetQueueAttributesResultForDlq() {
        return new GetQueueAttributesResult().withAttributes(Map.of(
                MESSAGES_ON_QUEUE.awsName, SOME_MESSAGES_ON_DLQ_COUNT.toString()));
    }

}