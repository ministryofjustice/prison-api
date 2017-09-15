package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.ImageRepository;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.ImageService;
import net.syscon.elite.v2.api.model.ImageDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {
    @Autowired
    private ImageRepository repository;

    @Override
    public ImageDetail findImageDetail(final Long imageId) {
        return repository.findImageDetail(imageId).orElseThrow(new EntityNotFoundException(String.valueOf(imageId)));
    }

    @Override
    public byte[] getImageContent(final Long imageId) {
        return repository.getImageContent(imageId);
    }
}
