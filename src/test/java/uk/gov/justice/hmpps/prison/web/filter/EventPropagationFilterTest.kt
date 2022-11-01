package uk.gov.justice.hmpps.prison.web.filter

import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import uk.gov.justice.hmpps.prison.util.MdcUtility.SUPPRESS_XTAG_EVENTS
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class EventPropagationFilterTest {

  private val request = mock<HttpServletRequest>()
  private val response = mock<HttpServletResponse>()
  private val filterChain = mock<FilterChain>()
  private val filter = EventPropagationFilter()

  @Test
  fun shouldNotSuppressEvents() {
    mockStatic(MDC::class.java).use { mockMdc ->
      filter.doFilter(request, response, filterChain)

      mockMdc.verify { MDC.put(SUPPRESS_XTAG_EVENTS, "false") }
      verify(request).getHeader("no-event-propagation")
      verify(filterChain).doFilter(request, response)
    }
  }

  @Test
  fun shouldSuppressEvents() {
    mockStatic(MDC::class.java).use { mockMdc ->
      whenever(request.getHeader(anyString())).thenReturn("true")

      filter.doFilter(request, response, filterChain)

      mockMdc.verify { MDC.put(SUPPRESS_XTAG_EVENTS, "true") }
      verify(request).getHeader("no-event-propagation")
      verify(filterChain).doFilter(request, response)
    }
  }
}
