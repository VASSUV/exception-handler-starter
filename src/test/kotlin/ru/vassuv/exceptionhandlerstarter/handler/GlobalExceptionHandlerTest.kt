package ru.vassuv.exceptionhandlerstarter.handler

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handles uncaught exception with default status`() {
        val response = handler.handleAllUncaughtException(RuntimeException("boom"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        val body = requireNotNull(response.body)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.status)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, body.error)
        assertEquals("boom", body.message)
        assertNotNull(body.classPath)
    }

    @Test
    fun `handles validation exception with field list`() {
        val bindingResult = BeanPropertyBindingResult(Any(), "payload").apply {
            addError(FieldError("payload", "firstName", "first name invalid"))
            addError(FieldError("payload", "lastName", "last name invalid"))
        }
        val exception = MethodArgumentNotValidException(dummyParameter(), bindingResult)

        val error = handler.handleValidationExceptions(exception)

        assertEquals(HttpStatus.BAD_REQUEST.value(), error.status)
        assertTrue(error.message!!.contains("firstName"))
        assertTrue(error.message.contains("lastName"))
    }

    @Test
    fun `handles json parse exception from invalid format`() {
        val jsonParser = JsonFactory().createParser("\"text\"")
        val invalidFormat = InvalidFormatException(jsonParser, "wrong format", "text", Int::class.java).apply {
            prependPath(null, "age")
        }
        val ex = HttpMessageNotReadableException("Body parse error", invalidFormat)

        val response = handler.handleJsonParseException(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = requireNotNull(response.body)
        assertEquals("Malformed JSON request", body.error)
        assertTrue(body.message!!.contains("age"))
        assertEquals(invalidFormat.originalMessage, body.originalMessage)
    }

    @Test
    fun `handles argument type mismatch`() {
        val mismatch = MethodArgumentTypeMismatchException(
            "abc",
            Int::class.java,
            "age",
            dummyParameter(),
            IllegalArgumentException("invalid type")
        )

        val response = handler.handleMethodArgumentTypeMismatchException(mismatch)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        val body = requireNotNull(response.body)
        assertEquals("Invalid request parameter", body.error)
        assertTrue(body.message!!.contains("age"))
        val expectedTypeName = mismatch.requiredType?.simpleName ?: "unknown"
        assertTrue(body.message.contains(expectedTypeName, ignoreCase = true))
    }

    @Test
    fun `handles exception with annotated status`() {
        val response = handler.handleAllUncaughtException(NotFoundException("nothing here"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        val body = requireNotNull(response.body)
        assertEquals(HttpStatus.NOT_FOUND.value(), body.status)
        assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, body.error)
        assertEquals("nothing here", body.message)
    }

    private fun dummyParameter(): MethodParameter =
        MethodParameter(DummyController::class.java.getDeclaredMethod("dummy", String::class.java), 0)

    private class DummyController {
        fun dummy(value: String) {}
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private class NotFoundException(message: String) : RuntimeException(message)
}
