package scott.infra.validacion;

import scott.infra.jpa.entidad.ErrorValidacion;
import scott.infra.jpa.entidad.Validable;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class Validador {

    final static protected Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <V extends Validable> void validarAtributos(V instancia) {
        validarAtributos(instancia, null);
    }

    public static <V extends Validable> void validarAtributos(V instancia, Consumer<List<ValorInvalido>> validacion) {
        final var violaciones = Validador.validator.validate(instancia);
        final var valoresInvalidos = new ArrayList<>(violaciones.stream()
                .map(ValorInvalido::new)
                .sorted(Comparator.comparing(ValorInvalido::nombre))
                .toList());
        if (validacion != null) {
            validacion.accept(valoresInvalidos);
        }
        if (!valoresInvalidos.isEmpty()) {
            throw new ErrorValidacion(instancia.getClass().getSimpleName(), valoresInvalidos);
        }
    }
}
