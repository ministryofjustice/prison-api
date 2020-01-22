package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.ImageRepository;
import net.syscon.elite.repository.jpa.model.OffenderImage;
import net.syscon.elite.repository.jpa.repository.OffenderImageRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private OffenderImageRepository offenderImageRepository;

    private ImageService service;

    @Before
    public void setUp() throws Exception {
        service = new ImageServiceImpl(imageRepository, offenderImageRepository);
    }

    @Test
    public void findOffenderImages() {

        OffenderImage image = mock(OffenderImage.class);

        when(offenderImageRepository.getImagesByOffenderNumber("A1234AA")).thenReturn(List.of(image));

        assertThat(service.findOffenderImagesFor("A1234AA")).containsOnly(image);
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

        assertThat(service.getImageContent(-1L, true)).isEqualTo(data);
    }

    @Test
    public void getImageContentForOffender() {

        byte[] data = new byte[]{0x12};

        when(imageRepository.getImageContent("A1234AA", true)).thenReturn(data);

        assertThat(service.getImageContent("A1234AA", true)).isEqualTo(data);
    }
}