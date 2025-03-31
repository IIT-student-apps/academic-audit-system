package by.bsuir.academicauditsystemgateway.exception;

public class UserOperationException extends RuntimeException {
    public UserOperationException(String message) {
        super(message);
    }

    public UserOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
