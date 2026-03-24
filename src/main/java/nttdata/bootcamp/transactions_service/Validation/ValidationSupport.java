package nttdata.bootcamp.transactions_service.Validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jakarta Bean Validation helper that aggregates violations into one message.
 */
@Component
@RequiredArgsConstructor
public class ValidationSupport {
    private final Validator validator;

    /**
     * Validates {@code value} or throws {@link ConstraintViolationException}.
     *
     * @param value object to validate
     * @param <T> type
     * @return same instance when valid
     */
    public <T> T validateOrThrow(T value) {
        Set<ConstraintViolation<T>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException(message, (Set) violations);
        }
        return value;
    }
}
