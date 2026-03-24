package nttdata.bootcamp.transactions_service.Exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /** IllegalArgument maps to 400. */
    @Test
    void handleIllegalArgument_returnsBadRequest() {
        ResponseEntity<Map<String, String>> res =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("bad", res.getBody().get("message"));
    }

    /** ConstraintViolation maps to 400. */
    @Test
    void handleConstraintViolation_returnsBadRequest() {
        ResponseEntity<Map<String, String>> res =
                handler.handleConstraintViolationException(
                        new ConstraintViolationException("violation", null));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    /** Runtime maps to 404 in this handler. */
    @Test
    void handleRuntime_returnsNotFound() {
        ResponseEntity<Map<String, String>> res =
                handler.handleRuntimeException(new RuntimeException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("missing", res.getBody().get("message"));
    }
}
