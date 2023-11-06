package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.storedprocs.OffenderAdminProcs.GenerateNewBookingNo

interface BookNumberGenerationService {
  fun generateBookNumber(): String
}

@Service
@Profile("nomis")
class BookNumberGenerationSPService(val generateNewBookingNo: GenerateNewBookingNo) : BookNumberGenerationService {
  override fun generateBookNumber(): String = generateNewBookingNo.executeFunction(String::class.java)
}

@Service
@Profile("!nomis")
class BookNumberGenerationBasicService : BookNumberGenerationService {
  override fun generateBookNumber(): String {
    // generate 5 digit random number ending in letter
    return (1..5).map { (Math.random() * 9).toInt() }.joinToString("") + "D"
  }
}
