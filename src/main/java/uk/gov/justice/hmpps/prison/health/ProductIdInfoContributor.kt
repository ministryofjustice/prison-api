package uk.gov.justice.hmpps.prison.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class ProductIdInfoContributor(@Value("\${product-id:default}") private val productId: String) : InfoContributor {

  override fun contribute(builder: Info.Builder) {
    builder.withDetail("productId", productId)
  }
}
