package luxdine.example.luxdine.exception;

public class EmployeeHasDependenciesException extends RuntimeException {
    public EmployeeHasDependenciesException(String message) {
        super(message);
    }
    
    public EmployeeHasDependenciesException(String message, Throwable cause) {
        super(message, cause);
    }
}

