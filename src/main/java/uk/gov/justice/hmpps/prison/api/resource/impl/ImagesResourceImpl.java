package uk.gov.justice.hmpps.prison.api.resource.impl;


import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.resource.ImageResource;
import uk.gov.justice.hmpps.prison.service.ImageService;

import java.util.List;

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
