package ru.vassuv.exceptionhandlerstarter.handler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import ru.vassuv.exceptionhandlerstarter.model.ErrorResponse

/**
 * Глобальный обработчик исключений.
 * Перехватывает ошибки и возвращает пользователю удобные сообщения.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Обработка всех исключений приложения.
     *
     * @param ex Исключение, возникающее при ошибке.
     * @return Ответ с описанием ошибок.
     */
    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ErrorResponse> {
        val status = ex.resolveAnnotatedStatus() ?: HttpStatus.INTERNAL_SERVER_ERROR
        val response = buildErrorResponse(
            status = status,
            message = ex.localizedMessage,
            classPath = ex.classPath,
            originalMessage = ex.cause?.message
        )
        logError(response, ex)
        return response.toResponseEntity(status)
    }

    /**
     * Обрабатывает исключения валидации.
     *
     * @param ex Исключение, возникающее при ошибке валидации.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ErrorResponse {
        val fieldErrors = ex.bindingResult.fieldErrors
        val message = when {
            fieldErrors.isEmpty() -> ex.localizedMessage
            fieldErrors.size == 1 -> "Некорректное значение в поле '${fieldErrors.first().field}'"
            else -> "Некорректные значения в полях: ${fieldErrors.joinToString(", ") { it.field }}"
        }
        return buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            message = message,
            classPath = ex.classPath,
            originalMessage = fieldErrors.firstOrNull()?.defaultMessage
        ).also { logError(it, ex) }
    }

    /**
     * Обрабатывает исключения парсинга json.
     *
     * @param ex Исключение, возникающее при ошибке парсинга.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val (message, originalMessage) = ex.describeJsonParsingError()
        val response = buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Malformed JSON request",
            message = message,
            classPath = ex.classPath,
            originalMessage = originalMessage
        )
        logError(response, ex)
        return response.toResponseEntity(HttpStatus.BAD_REQUEST)
    }

    /**
     * Обрабатывает исключения некорректных значений параметров.
     *
     * @param ex Исключение, возникающее при некорректных параметрах.
     * @return Ответ с описанием ошибок.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {

        val fieldName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "unknown"
        val invalidValue = ex.value

        val message = "Некорректное значение параметра '$fieldName'. " +
                "Ожидался тип '$requiredType', получено значение '$invalidValue'."

        val response = buildErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Invalid request parameter",
            message = message,
            classPath = ex.classPath
        )
        logError(response, ex)
        return response.toResponseEntity(HttpStatus.BAD_REQUEST)
    }

    /**
     * Получает путь до класса вместе с его названием, в котором была ошибка
     */
    private val Throwable.classPath: String
        get() = this.stackTrace.firstOrNull()?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"

    private fun buildErrorResponse(
        status: HttpStatus,
        error: String = status.reasonPhrase.ifBlank { status.name },
        message: String?,
        classPath: String,
        originalMessage: String? = null
    ) = ErrorResponse(
        status = status.value(),
        error = error,
        message = message,
        classPath = classPath,
        originalMessage = originalMessage
    )

    private fun ErrorResponse.toResponseEntity(status: HttpStatus) = ResponseEntity(this, status)

    private fun logError(error: ErrorResponse, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error(
                "Unexpected exception at [{}]: status={}, error={}, message={}, original={}",
                error.classPath,
                error.status,
                error.error,
                error.message,
                error.originalMessage,
                throwable
            )
        } else {
            logger.error(
                "Unexpected exception at [{}]: status={}, error={}, message={}, original={}",
                error.classPath,
                error.status,
                error.error,
                error.message,
                error.originalMessage
            )
        }
    }

    /**
     * Поиск статуса из аннотации к ошибке
     *
     * @receiver Exception проверяемая ошибка
     * @return Статус указанный в аннотацие
     */
    private fun Exception.resolveAnnotatedStatus(): HttpStatus? =
        AnnotationUtils.findAnnotation(this.javaClass, ResponseStatus::class.java)?.code

    private fun HttpMessageNotReadableException.describeJsonParsingError(): Pair<String?, String?> {
        val rootCause = cause
        return when (rootCause) {
            is InvalidFormatException -> {
                "Некорректный формат поля '${rootCause.fieldPath()}'" to rootCause.originalMessage
            }

            is MismatchedInputException -> {
                "Неправильный тип данных поля '${rootCause.fieldPath()}'" to rootCause.originalMessage
            }

            else -> localizedMessage to rootCause?.message
        }
    }

    private fun InvalidFormatException.fieldPath(): String =
        path.joinToString(".") { it.renderPathSegment() }

    private fun MismatchedInputException.fieldPath(): String =
        path.joinToString(".") { it.renderPathSegment() }

    private fun com.fasterxml.jackson.databind.JsonMappingException.Reference.renderPathSegment(): String =
        fieldName ?: index.let { "[$it]" }
}
