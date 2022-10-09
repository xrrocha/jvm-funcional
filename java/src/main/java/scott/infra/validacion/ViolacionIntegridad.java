package scott.infra.validacion;

public class ViolacionIntegridad extends RuntimeException {
    public ViolacionIntegridad(String message) {
        super(message);
    }

    public ViolacionIntegridad(String message, Throwable cause) {
        super(message, cause);
    }
}
