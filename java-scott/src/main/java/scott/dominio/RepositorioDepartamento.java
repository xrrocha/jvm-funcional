package scott.dominio;

import org.springframework.stereotype.Repository;
import scott.infra.jpa.Repositorio;

import java.util.Optional;

@Repository
public interface RepositorioDepartamento extends Repositorio<Departamento> {
    Optional<Departamento> findByCodigo(String codigo);

    default Optional<Departamento> buscarPorCodigo(String codigo) {
        return findByCodigo(codigo);
    }
}
