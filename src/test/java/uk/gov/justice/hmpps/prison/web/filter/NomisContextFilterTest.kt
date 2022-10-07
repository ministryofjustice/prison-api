package uk.gov.justice.hmpps.prison.web.filter

import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.MERGE
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.PRISON_API
import uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_CONTEXT
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NomisContextFilterTest {

  private val request = mock<HttpServletRequest>()
  private val response = mock<HttpServletResponse>()
  private val filterChain = mock<FilterChain>()
  private val filter = NomisContextFilter()

  @Test
  fun shouldUsePrisonApiContext() {
    mockStatic(MDC::class.java).use { mockMdc ->
      filter.doFilter(request, response, filterChain)

      mockMdc.verify { MDC.put(NOMIS_CONTEXT, PRISON_API.name) }
      verify(request).getHeader("no-event-propagation")
      verify(filterChain).doFilter(request, response)
    }
  }

  @Test
  fun shouldUseMergeContext() {
    mockStatic(MDC::class.java).use { mockMdc ->
      whenever(request.getHeader(anyString())).thenReturn("true")

      filter.doFilter(request, response, filterChain)

      mockMdc.verify { MDC.put(NOMIS_CONTEXT, MERGE.name) }
      verify(request).getHeader("no-event-propagation")
      verify(filterChain).doFilter(request, response)
    }
  }
}
