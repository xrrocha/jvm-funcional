package scott.infra.jpa.entidad;

import scott.infra.validacion.Validador;
import scott.infra.validacion.ValorInvalido;

import java.util.List;
import java.util.function.Consumer;

public abstract class Validable {

    protected void validarAtributos() {
        Validador.validarAtributos(this, null);
    }

    protected void validarInstancia(Consumer<List<ValorInvalido>> validacion) {
        validarAtributos();
        Validador.validarAtributos(this, validacion);
    }
}
