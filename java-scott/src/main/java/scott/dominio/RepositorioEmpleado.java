package scott.dominio;

import scott.infra.jpa.Repositorio;

import java.util.Optional;

public interface RepositorioEmpleado extends Repositorio<Empleado> {
    Optional<Empleado> findByCodigo(String codigo);

    default Optional<Empleado> buscarPorCodigo(String codigo) {
        return findByCodigo(codigo);
    }
}
