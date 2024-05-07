package ch.steinerschule_stgallen.billing;

public class NoKeysException extends RuntimeException{
    public NoKeysException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
    public NoKeysException(String errorMessage) {
        super(errorMessage);
    }
}
