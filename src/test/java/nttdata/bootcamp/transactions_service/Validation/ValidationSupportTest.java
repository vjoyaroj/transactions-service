package nttdata.bootcamp.transactions_service.Validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ValidationSupport}.
 */
class ValidationSupportTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ValidationSupport support = new ValidationSupport(validator);

    @Data
    private static class Sample {
        @NotBlank
        private String name;
    }

    /** Invalid bean throws. */
    @Test
    void validateOrThrow_whenInvalid_throws() {
        assertThrows(ConstraintViolationException.class, () -> support.validateOrThrow(new Sample()));
    }

    /** Valid bean returns same reference. */
    @Test
    void validateOrThrow_whenValid_returnsSame() {
        Sample s = new Sample();
        s.setName("ok");
        assertEquals(s, support.validateOrThrow(s));
    }
}
