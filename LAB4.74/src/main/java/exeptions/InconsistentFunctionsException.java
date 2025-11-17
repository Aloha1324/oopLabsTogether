package exeptions;

public class InconsistentFunctionsException extends RuntimeException {

    public InconsistentFunctionsException() {
        super();
    }

    public InconsistentFunctionsException(String message) {
        super(message);
    }
}
