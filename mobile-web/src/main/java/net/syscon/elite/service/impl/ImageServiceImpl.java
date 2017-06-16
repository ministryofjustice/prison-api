package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.ImageRepository;
import net.syscon.elite.service.ImageService;
import net.syscon.elite.web.api.model.ImageDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    private final ImageRepository repository;

    @Inject
    public ImageServiceImpl(ImageRepository repository) {
        this.repository = repository;
    }

    @Override
    public ImageDetails findImageDetail(final Long imageId) {
        return repository.findImageDetail(imageId);
    }

    @Override
    public byte[] getImageContent(final Long imageId) {
        return repository.getImageContent(imageId);
    }
}
