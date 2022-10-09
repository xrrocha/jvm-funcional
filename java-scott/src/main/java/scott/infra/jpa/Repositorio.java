package scott.infra.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import scott.infra.jpa.entidad.Entidad;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Repositorio<E extends Entidad> extends JpaRepository<E, String> {

    default E guardar(E entidad) {
        return save(entidad);
    }

    default E guardarYGrabar(E entidad) {
        return saveAndFlush(entidad);
    }

    default E leerPorId(String id) {
        return getById(id);
    }

    default Optional<E> buscarPorId(String id) {
        return findById(id);
    }

    default List<E> leerTodos() {
        return findAll();
    }

    default Stream<E> encontrar(Predicate<E> predicado) {
        return findAll().stream().filter(predicado);
    }

    private static String nombreEntidad(Class<?> clase) {
        String nombre = clase.getSimpleName();
        if (nombre.startsWith("Repositorio")) {
            return nombre.substring(10);
        }
        return nombre;
    }
}
