package by.bsuir.academicauditsystemgateway.exception;

public class DocumentAnalyzeRequestOperationException extends RuntimeException {
    public DocumentAnalyzeRequestOperationException(String message) {
        super(message);
    }

    public DocumentAnalyzeRequestOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
