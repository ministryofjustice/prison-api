package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.ImageRepository;
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    private static final LocalDateTime DATETIME = LocalDateTime.now();
    private static final String OFFENDER_NUMBER = "A1234AA";

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private OffenderImageRepository offenderImageRepository;

    @Mock
    private PrisonerRepository prisonerRepository;

    private ImageService service;

    @BeforeEach
    public void setUp() {
        service = new ImageService(imageRepository, offenderImageRepository, prisonerRepository);
    }

    @Test
    public void findOffenderImages() {

        when(prisonerRepository.getOffenderIdsFor(OFFENDER_NUMBER)).thenReturn(Set.of(1L));

        when(offenderImageRepository.getImagesByOffenderNumber(OFFENDER_NUMBER)).thenReturn(List.of(
                OffenderImage.builder()
                        .offenderImageId(123L)
                        .captureDateTime(DATETIME)
                        .imageViewType("FACE")
                        .orientationType("FRONT")
                        .imageObjectType("OFF_BKG")
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

        when(prisonerRepository.getOffenderIdsFor(OFFENDER_NUMBER)).thenReturn(emptySet());

        assertThatThrownBy(() -> service.findOffenderImagesFor(OFFENDER_NUMBER))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void findImageDetail() {

        ImageDetail imageDetail = mock(ImageDetail.class);

        when(imageRepository.findImageDetail(-1L)).thenReturn(Optional.of(imageDetail));

        assertThat(service.findImageDetail(-1L)).isEqualTo(imageDetail);
    }

    @Test
    public void findImageDetailThrowsEntityNotFoundException() {

        when(imageRepository.findImageDetail(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findImageDetail(-1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getImageContent() {

        byte[] data = new byte[]{0x12};

        when(imageRepository.getImageContent(-1L, true)).thenReturn(data);

        assertThat(service.getImageContent(-1L, true)).isNotEmpty();
        assertThat(service.getImageContent(-1L, true)).get().isEqualTo(data);
    }

    @Test
    public void getImageContentForOffender() {

        byte[] data = new byte[]{0x12};

        when(imageRepository.getImageContent("A1234AA", true)).thenReturn(data);

        assertThat(service.getImageContent("A1234AA", true)).get().isEqualTo(data);
    }
}
