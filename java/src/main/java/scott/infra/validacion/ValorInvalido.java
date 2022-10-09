package scott.infra.validacion;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.Objects;
import java.util.stream.StreamSupport;

public record ValorInvalido(String nombre, String valorInvalido, String mensaje) {

    public ValorInvalido(ConstraintViolation<?> violacion) {
        this(nombrePath(violacion.getPropertyPath()),
                violacion.getInvalidValue() == null ? "null" : violacion.getInvalidValue().toString(),
                violacion.getMessage());
    }

    public ValorInvalido(String nombre, Object valorInvalido, String mensaje) {
        this(nombre, valorInvalido == null ? "null" : valorInvalido.toString(), mensaje);
    }

    static String nombrePath(Path path) {
        final var listaNodos =
                StreamSupport.stream(path.spliterator(), false)
                        .filter(Objects::nonNull)
                        .map(Path.Node::getName)
                        .toList();
        if (listaNodos.isEmpty()) {
            return path.toString();
        }
        return listaNodos.get(listaNodos.size() - 1);
    }

    @Override
    public String toString() {
        return "Valor inválido para %s: %s (%s)".formatted(nombre(), mensaje(), valorInvalido());
    }
}
