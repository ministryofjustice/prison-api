package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    private static final LocalDateTime DATETIME = LocalDateTime.now();
    private static final String OFFENDER_NUMBER = "A1234AA";

    @Mock
    private OffenderImageRepository offenderImageRepository;

    @Mock
    private OffenderRepository offenderRepository;

    private ImageService service;

    @BeforeEach
    public void setUp() {
        service = new ImageService(offenderImageRepository, offenderRepository);
    }

    @Test
    public void findOffenderImages() {

        when(offenderRepository.findByNomsId(OFFENDER_NUMBER)).thenReturn(List.of(Offender.builder().id(1L).build()));

        when(offenderImageRepository.getImagesByOffenderNumber(OFFENDER_NUMBER)).thenReturn(List.of(
                OffenderImage.builder()
                        .id(123L)
                        .captureDateTime(DATETIME)
                        .viewType("FACE")
                        .orientationType("FRONT")
                        .imageType("OFF_BKG")
                        .imageObjectId(1L)
                        .build()));

        assertThat(service.findOffenderImagesFor(OFFENDER_NUMBER)).containsOnly(
                ImageDetail.builder()
                        .imageId(123L)
                        .captureDate(DATETIME.toLocalDate())
                        .imageView("FACE")
                        .imageOrientation("FRONT")
                        .imageType("OFF_BKG")
                        .objectId(1L)
                        .build());
    }

    @Test
    public void findOffenderImagesThrowsEntityNotFoundException() {

        when(offenderRepository.findByNomsId(OFFENDER_NUMBER)).thenReturn(List.of());

        assertThatThrownBy(() -> service.findOffenderImagesFor(OFFENDER_NUMBER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getImageContent() {

        byte[] data = new byte[]{0x12};

        when(offenderImageRepository.findById(-1L)).thenReturn(Optional.of(OffenderImage.builder().id(-1L).fullSizeImage(data).build()));

        assertThat(service.getImageContent(-1L, true)).isNotEmpty();
        assertThat(service.getImageContent(-1L, true)).get().isEqualTo(data);
    }

    @Test
    public void getImageContentForOffender() {

        byte[] data = new byte[]{0x12};

        when(offenderImageRepository.findLatestByOffenderNumber("A1234AA")).thenReturn(Optional.of(OffenderImage.builder().id(-1L).fullSizeImage(data).build()));

        assertThat(service.getImageContent("A1234AA", true)).get().isEqualTo(data);
    }
}
