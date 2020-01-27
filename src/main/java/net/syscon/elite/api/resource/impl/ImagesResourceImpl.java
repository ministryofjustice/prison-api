package net.syscon.elite.api.resource.impl;


import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.ImageDetail;
import net.syscon.elite.api.resource.ImageResource;
import net.syscon.elite.service.ImageService;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.base.path}/images")
@AllArgsConstructor
public class ImagesResourceImpl implements ImageResource {

    private final ImageService imageService;

    @Override
    public ResponseEntity<byte[]> getImageData(final Long imageId, final boolean fullSizeImage) {
        return imageService.getImageContent(imageId, fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @Override
    public List<ImageDetail> getImagesByOffender(final String offenderNo) {
        return imageService.findOffenderImagesFor(offenderNo);
    }

    @Override
    public ImageDetail getImage(final Long imageId) {
        return imageService.findImageDetail(imageId);
    }
}
