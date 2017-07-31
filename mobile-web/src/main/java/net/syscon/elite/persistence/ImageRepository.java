package net.syscon.elite.persistence;


import net.syscon.elite.web.api.model.ImageDetails;

import java.util.Optional;

public interface ImageRepository {

	Optional<ImageDetails> findImageDetail(Long imageId);
	byte[] getImageContent(Long imageId);


}

