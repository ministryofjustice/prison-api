package net.syscon.elite.service;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.repository.ReferenceCodeRepository;
import net.syscon.elite.service.support.ReferenceDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
public class ReferenceDomainServiceImplTest {

    @Mock
    private ReferenceCodeRepository repository;

    private ReferenceDomainService service;

    @BeforeEach
    public void setUp() {
        initMocks(repository);
        service = new ReferenceDomainService(repository);
    }

    @Test
    public void testScheduleReasons() {

        when(repository.getReferenceCodeByDomainAndCode(
                eq(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.getDomain()),
                eq("APP"),
                eq(false)))
                .thenReturn(
                        Optional.of(ReferenceCode.builder()
                                .domain(ReferenceDomain.INTERNAL_SCHEDULE_TYPE.getDomain())
                                .build()));

        when(repository.getScheduleReasons(eq("APP")))
                .thenReturn(Arrays.asList(
                        ReferenceCode.builder().code("CODE1").description("HELLO To something").build(),
                        ReferenceCode.builder().code("CODE2").description("goodbye To love").build(),
                        ReferenceCode.builder().code("CODE3").description("apple").build(),
                        ReferenceCode.builder().code("CODE4").description("zebra").build(),
                        ReferenceCode.builder().code("CODE5").description("TReVOR").build(),
                        ReferenceCode.builder().code("CODE6").description("COMPUTERS").build()
                ));

        final var scheduleReasons = service.getScheduleReasons("APP");

        assertThat(scheduleReasons.size()).isEqualTo(6);
        assertThat(scheduleReasons.get(0).getDescription()).isEqualTo("Apple");
        assertThat(scheduleReasons.get(1).getDescription()).isEqualTo("Computers");
        assertThat(scheduleReasons.get(2).getDescription()).isEqualTo("Goodbye To Love");
        assertThat(scheduleReasons.get(3).getDescription()).isEqualTo("Hello To Something");
        assertThat(scheduleReasons.get(4).getDescription()).isEqualTo("Trevor");
        assertThat(scheduleReasons.get(5).getDescription()).isEqualTo("Zebra");
    }

    @Test
    public void testReferenceCodeIsActive() {
        when(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "APPROVED", false))
                .thenReturn(Optional.of(ReferenceCode.builder().activeFlag("Y").build()));
        assertThat(service.isReferenceCodeActive("HDC_APPROVE", "APPROVED")).isTrue();
    }

    @Test
    public void testReferenceCodeIsNotActive() {
        when(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "DISABLED", false))
                .thenReturn(Optional.of(ReferenceCode.builder().activeFlag("N").build()));
        assertThat(service.isReferenceCodeActive("HDC_APPROVE", "DISABLED")).isFalse();
    }

    @Test
    public void testReferenceCodeIsNotPresent() {
        when(repository.getReferenceCodeByDomainAndCode("HDC_APPROVE", "APPROVED", false))
                .thenReturn(Optional.empty());
        assertThat(service.isReferenceCodeActive("HDC_APPROVE", "APPROVED")).isFalse();
    }

}
