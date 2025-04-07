package by.bsuir.academicauditsystemgateway.exception;

public class DocumentOperationException extends RuntimeException {

    public DocumentOperationException(String message) {
        super(message);
    }

    public DocumentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
