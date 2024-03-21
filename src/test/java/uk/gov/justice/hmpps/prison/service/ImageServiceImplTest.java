package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    private static final LocalDateTime DATETIME = LocalDateTime.now();
    private static final String OFFENDER_NUMBER = "A1234AA";

    private final byte[] imageData = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAAAAACH5BAAAAAAALAAAAAABAAEAAAICTAEAOw==");

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
                        .active(false)
                        .captureDateTime(DATETIME)
                        .viewType("FACE")
                        .orientationType("FRONT")
                        .imageType("OFF_BKG")
                        .imageObjectId(1L)
                        .build()));

        assertThat(service.findOffenderImagesFor(OFFENDER_NUMBER)).containsOnly(
                ImageDetail.builder()
                        .imageId(123L)
                        .active(false)
                        .captureDate(DATETIME.toLocalDate())
                        .captureDateTime(DATETIME)
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

    @Test
    public void putImageForOffenderNotFound() {

        when(offenderRepository.findOffenderWithLatestBookingByNomsId(OFFENDER_NUMBER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.putImageForOffender(OFFENDER_NUMBER, new ByteArrayInputStream(imageData)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("No prisoner found for prisoner number %s", OFFENDER_NUMBER);
    }

    @Test
    public void putImageForOffenderWithNoBooking() {

        Offender offenderAndBooking = Offender.builder().id(1L).build();

        when(offenderRepository.findOffenderWithLatestBookingByNomsId(OFFENDER_NUMBER)).thenReturn(Optional.of(offenderAndBooking));

        assertThatThrownBy(() -> service.putImageForOffender(OFFENDER_NUMBER, new ByteArrayInputStream(imageData)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("There are no bookings for %s", OFFENDER_NUMBER);
    }

    @Test
    public void putImageForOffenderOk() {

        Offender offenderAndBooking = Offender.builder().id(1L).bookings(
            List.of(OffenderBooking.builder().bookingId(1L).bookingSequence(1).build())
        ).build();

        var newImage = OffenderImage
            .builder()
            .captureDateTime(LocalDateTime.now())
            .orientationType("FRONT")
            .viewType("FACE")
            .imageType("OFF_BKG")
            .active(true)
            .sourceCode("GEN")
            .offenderBooking(offenderAndBooking.getLatestBooking().isPresent() ? offenderAndBooking.getLatestBooking().get() : null)
            .thumbnailImage(imageData)
            .fullSizeImage(imageData)
            .build();


        when(offenderRepository.findOffenderWithLatestBookingByNomsId(OFFENDER_NUMBER)).thenReturn(Optional.of(offenderAndBooking));
        when(offenderImageRepository.findLatestByBookingId(1L)).thenReturn(Optional.empty());
        when(offenderImageRepository.save(newImage)).thenReturn(newImage);

        ImageDetail savedImage = service.putImageForOffender(OFFENDER_NUMBER, new ByteArrayInputStream(imageData));

        assertThat(savedImage).isEqualTo(newImage.transform());
    }

    @Test
    public void putImageUpdatesPreviousToInactive() {

        Offender offenderAndBooking = Offender.builder().id(1L).bookings(
            List.of(OffenderBooking.builder().bookingId(1L).bookingSequence(1).build())
        ).build();

        var prevImage = OffenderImage
            .builder()
            .captureDateTime(LocalDateTime.now())
            .orientationType("FRONT")
            .viewType("FACE")
            .imageType("OFF_BKG")
            .active(false)
            .sourceCode("GEN")
            .offenderBooking(offenderAndBooking.getLatestBooking().isPresent() ? offenderAndBooking.getLatestBooking().get() : null)
            .thumbnailImage(imageData)
            .fullSizeImage(imageData)
            .build();

        var newImage = OffenderImage
            .builder()
            .captureDateTime(LocalDateTime.now())
            .orientationType("FRONT")
            .viewType("FACE")
            .imageType("OFF_BKG")
            .active(true)
            .sourceCode("GEN")
            .offenderBooking(offenderAndBooking.getLatestBooking().isPresent() ? offenderAndBooking.getLatestBooking().get() : null)
            .thumbnailImage(imageData)
            .fullSizeImage(imageData)
            .build();


        when(offenderRepository.findOffenderWithLatestBookingByNomsId(OFFENDER_NUMBER)).thenReturn(Optional.of(offenderAndBooking));
        when(offenderImageRepository.findLatestByBookingId(1L)).thenReturn(Optional.of(prevImage));
        when(offenderImageRepository.save(prevImage)).thenReturn(prevImage);
        when(offenderImageRepository.save(newImage)).thenReturn(newImage);

        ImageDetail savedImage = service.putImageForOffender(OFFENDER_NUMBER, new ByteArrayInputStream(imageData));

        assertThat(savedImage).isEqualTo(newImage.transform());
    }
}
