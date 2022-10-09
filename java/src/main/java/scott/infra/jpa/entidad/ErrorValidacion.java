package scott.infra.jpa.entidad;

import scott.infra.validacion.ValorInvalido;

import java.util.List;
import java.util.stream.Collectors;

public final class ErrorValidacion extends IllegalArgumentException {
    private final List<ValorInvalido> valoresInvalidos;

    public ErrorValidacion(String nombreClase, List<ValorInvalido> valoresInvalidos) {
        super("%s: %d error(es) de validación. %s".formatted(
                nombreClase, valoresInvalidos.size(), concatenarMensajesError(valoresInvalidos)));
        this.valoresInvalidos = valoresInvalidos;
    }

    // FIXME Se generar doblemente los errores de validación
    static String concatenarMensajesError(List<ValorInvalido> valoresInvalidos) {
        return valoresInvalidos.stream()
                .map(vi -> "%s %s: %s".formatted(vi.nombre(), vi.mensaje(), vi.valorInvalido()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public List<ValorInvalido> valoresInvalidos() {
        return valoresInvalidos;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
