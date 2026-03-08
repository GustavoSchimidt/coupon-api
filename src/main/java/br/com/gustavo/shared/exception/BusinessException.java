package br.com.gustavo.shared.exception;

public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message) {
        this(422, message);
    }

    public int getStatus() {
        return status;
    }
}       