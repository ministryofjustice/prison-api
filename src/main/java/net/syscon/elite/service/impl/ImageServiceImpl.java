package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.repository.ImageRepository;
import net.syscon.elite.repository.jpa.model.OffenderImage;
import net.syscon.elite.repository.jpa.repository.OffenderImageRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository repository;

    @Autowired
    private OffenderImageRepository offenderImageRepository;

    @Override
    public List<OffenderImage> findOffenderImagesFor(final String offenderNumber) {
        return offenderImageRepository.getImagesByOffenderNumber(offenderNumber);
    }

    @Override
    public ImageDetail findImageDetail(final Long imageId) {
        return repository.findImageDetail(imageId).orElseThrow(EntityNotFoundException.withId(imageId));
    }

    @Override
    public byte[] getImageContent(final Long imageId, final boolean fullSizeImage) {
        return repository.getImageContent(imageId, fullSizeImage);
    }

    @Override
    public byte[] getImageContent(final String offenderNo, final boolean fullSizeImage) {
        return repository.getImageContent(offenderNo, fullSizeImage);
    }
}
