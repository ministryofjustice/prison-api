package uk.gov.justice.hmpps.prison.web.handler

import jakarta.persistence.EntityExistsException
import jakarta.validation.ValidationException
import org.hibernate.exception.GenericJDBCException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.ConflictingRequestException
import uk.gov.justice.hmpps.prison.service.EntityAlreadyExistsException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.NoContentException
import java.sql.SQLException
import java.util.stream.Collectors

@RestControllerAdvice(basePackages = ["uk.gov.justice.hmpps.prison.api.resource"])
class ControllerAdvice {
  @ExceptionHandler(RestClientResponseException::class)
  fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<ByteArray> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(e.statusCode)
      .body(e.responseBodyAsByteArray)
  }

  @ExceptionHandler(RestClientException::class)
  fun handleRestClientException(e: RestClientException): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .developerMessage(e.mostSpecificCause.message)
          .build(),
      )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.FORBIDDEN.value())
          .build(),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.debug("Bad Request (400) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleValidationException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
    log.debug("Bad Request (400) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
    log.debug("Not found (404) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.NOT_FOUND.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(EntityAlreadyExistsException::class)
  fun handleEntityAlreadyExistsException(e: EntityAlreadyExistsException): ResponseEntity<ErrorResponse> {
    log.debug("Already exists (409) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.CONFLICT.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(ConflictingRequestException::class)
  fun handleConflictingRequestException(e: ConflictingRequestException): ResponseEntity<ErrorResponse> {
    log.debug("Conflict (409) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.CONFLICT.value())
          .errorCode(e.errorCode)
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(EmptyResultDataAccessException::class)
  fun handleEmptyResultDataAccessException(e: EmptyResultDataAccessException): ResponseEntity<ErrorResponse> =
    ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.NOT_FOUND.value())
          .developerMessage(e.message)
          .build(),
      )

  @ExceptionHandler(jakarta.persistence.EntityNotFoundException::class)
  fun handleEntityNotFoundException(e: jakarta.persistence.EntityNotFoundException): ResponseEntity<ErrorResponse> =
    ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.NOT_FOUND.value())
          .developerMessage(e.cause?.message)
          .build(),
      )

  @ExceptionHandler(EntityExistsException::class)
  fun handleEntityExistsException(e: EntityExistsException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.CONFLICT)
    .body(
      ErrorResponse
        .builder()
        .userMessage(e.message)
        .status(HttpStatus.CONFLICT.value())
        .developerMessage(e.message)
        .build(),
    )

  @ExceptionHandler(HttpClientErrorException::class)
  fun handleHttpClientErrorException(e: HttpClientErrorException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(e.statusCode)
    .body(
      ErrorResponse
        .builder()
        .userMessage(e.statusText)
        .status(e.statusCode.value())
        .developerMessage(e.mostSpecificCause.message)
        .build(),
    )

  @ExceptionHandler(TypeMismatchException::class)
  fun handleTypeMismatchException(e: TypeMismatchException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(
      ErrorResponse
        .builder()
        .userMessage(e.mostSpecificCause.message)
        .status(HttpStatus.BAD_REQUEST.value())
        .developerMessage(e.mostSpecificCause.message)
        .build(),
    )

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(
      ErrorResponse
        .builder()
        .userMessage(e.message)
        .status(HttpStatus.BAD_REQUEST.value())
        .developerMessage(e.message)
        .build(),
    )

  @ExceptionHandler(IllegalStateException::class)
  fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.BAD_REQUEST)
    .body(
      ErrorResponse
        .builder()
        .userMessage(e.message)
        .status(HttpStatus.BAD_REQUEST.value())
        .developerMessage(e.message)
        .build(),
    )

  @ExceptionHandler(InvalidDataAccessApiUsageException::class)
  fun handleInvalidDataAccessApiUsageException(e: InvalidDataAccessApiUsageException): ResponseEntity<ErrorResponse> =
    ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.mostSpecificCause.message)
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage(e.mostSpecificCause.message)
          .build(),
      )

  @ExceptionHandler(HttpMessageConversionException::class)
  fun handleHttpMessageConversionException(e: HttpMessageConversionException): ResponseEntity<ErrorResponse> {
    log.error("A malformed request was rejected due to exception", e.mostSpecificCause)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage("Malformed request")
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage("Malformed request")
          .build(),
      )
  }

  @ExceptionHandler(NoContentException::class)
  fun handleNoContentException(e: NoContentException): ResponseEntity<ErrorResponse> {
    log.debug("No content (204) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.NO_CONTENT)
      .body(
        ErrorResponse
          .builder()
          .userMessage("No content returned")
          .status(HttpStatus.NO_CONTENT.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    val errors = e.bindingResult.fieldErrors
      .stream()
      .map { error: FieldError -> "Field: " + error.field + " - " + error.defaultMessage }
      .collect(Collectors.joining(", "))
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage(errors)
          .status(HttpStatus.BAD_REQUEST.value())
          .developerMessage(errors)
          .build(),
      )
  }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(BadRequestException::class)
  fun handleServiceException(e: BadRequestException): ResponseEntity<ErrorResponse> {
    log.debug("Bad Request (400) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.BAD_REQUEST.value())
          .errorCode(e.errorCode)
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(DuplicateKeyException::class)
  fun handleServiceException(e: DuplicateKeyException): ResponseEntity<ErrorResponse> {
    log.debug("Conflict (409) returned with message {}", e.message)
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(
        ErrorResponse
          .builder()
          .userMessage(e.message)
          .status(HttpStatus.CONFLICT.value())
          .developerMessage(e.message)
          .build(),
      )
  }

  @ExceptionHandler(JpaSystemException::class)
  fun handleJpaException(e: JpaSystemException): ResponseEntity<ErrorResponse> {
    try {
      if (e.cause is GenericJDBCException && (e.cause as GenericJDBCException).cause is SQLException) {
        return handleJpaSqlException(e, e.cause!!.cause!!)
      }
    } catch (ex: Exception) {
      // we don't know the shape of this exception - handling below
      log.error("Caught error trying to parse jpa sql exception", ex)
    }

    // If this ever happens we should investigate and see if we can handle it in a nicer way
    log.info("Error received from JPA caused by {}", e.rootCause, e)
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .userMessage("An unexpected error occurred")
          .developerMessage("Root cause: ${e.rootCause}")
          .build(),
      )
  }

  private fun handleJpaSqlException(e: JpaSystemException, rootCause: Throwable): ResponseEntity<ErrorResponse> {
    log.info("Error received from JPA caused by {}", rootCause.message, e)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse
          .builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .userMessage(getJpaSqlExceptionUserMessage(rootCause))
          .developerMessage(getJpaSqlExceptionDeveloperMessage(rootCause))
          .build(),
      )
  }

  private fun getJpaSqlExceptionUserMessage(rootCause: Throwable): String = rootCause
    .message!!
    .split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    .split("^ORA-\\d{5,6}: ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

  private fun getJpaSqlExceptionDeveloperMessage(rootCause: Throwable): String? = rootCause.message

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
