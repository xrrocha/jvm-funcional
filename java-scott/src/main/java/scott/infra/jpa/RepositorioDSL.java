package scott.infra.jpa;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.control.Either;
import org.springframework.data.jpa.repository.JpaRepository;
import scott.infra.Falla;
import scott.infra.Falla.FallaAplicacion;
import scott.infra.Falla.FallaSistema;

import java.util.Optional;
import java.util.function.Function;

public class RepositorioDSL {

    public static <E, I> Either<Falla, I> persistirInstancia(
            JpaRepository<E, I> repositorio,
            CheckedFunction1<E, I> clavePrimaria,
            CheckedConsumer<E> validacion,
            CheckedFunction0<E> crearInstancia
    ) {
        return eitherCatch("creando instancia de entidad en memoria", crearInstancia)
                .flatMap(entidad ->
                        eitherCatch("validando instancia de entidad en memoria", entidad, validacion))
                .flatMap(entidad ->
                        eitherCatch("persistiendo nueva instancia", () -> repositorio.save(entidad)))
                .flatMap(entidad ->
                        eitherCatch("recuperando clave primaria", () -> clavePrimaria.apply(entidad)));
    }

    public static <E, I> Either<Falla, Void> actualizar(
            I id,
            JpaRepository<E, I> repositorio,
            CheckedConsumer<E> actualizar
    ) {
        try {
            return repositorio
                    .findById(id)
                    .map(entidad ->
                            eitherCatch("actualizando entidad", entidad, actualizar)
                                    .peek(repositorio::saveAndFlush)
                                    .<Void>map(e -> null)
                    )
                    .orElseGet(() -> Either.left(new FallaAplicacion("Id no encontrado: %s".formatted(id))));
        } catch (Throwable t) {
            return Either.left(new FallaSistema("Error inesperado actualizando entidad", t));
        }
    }

    public static <E, I, R> Either<Falla, R> actualizarConResultado(
            I id,
            JpaRepository<E, I> repositorio,
            CheckedFunction1<E, R> actualizar
    ) {
        try {
            return repositorio
                    .findById(id)
                    .map(entidad ->
                            eitherCatch("actualizando entidad", () -> actualizar.apply(entidad))
                                    .flatMap(resultado ->
                                            eitherCatch("grabando entidad actualizada",
                                                    () -> repositorio.saveAndFlush(entidad))
                                                    .map(ignored -> resultado)
                                    )
                    )
                    .orElseGet(() -> Either.left(new FallaAplicacion("Id no encontrado: %s".formatted(id))));
        } catch (Throwable t) {
            return Either.left(new FallaSistema("Error inesperado actualizando entidad", t));
        }
    }

    public static <E, I> E leer(JpaRepository<E, I> repositorio, I id) {
        return Optional.ofNullable(id)
                .flatMap(repositorio::findById)
                .orElseThrow(() -> new RuntimeException("Id inexistente: %s".formatted(id)));
    }

    public static <E, C> E leer(Function<C, Optional<E>> lector, C clave) {
        return Optional.ofNullable(clave)
                .flatMap(lector)
                .orElseThrow(() -> new RuntimeException("Id inexistente: %s".formatted(clave)));
    }

    public static <E, I> E leerOpcional(JpaRepository<E, I> repositorio, I id) {
        if (id == null) return null;
        else return repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Id inexistente: %s".formatted(id)));
    }

    public static <E, C>
    CheckedConsumer<E> detectarDuplicado(Function<C, Optional<E>> extractor, C valorClave) {
        return e -> extractor.apply(valorClave).ifPresent(t -> {
            throw new RuntimeException("Ya existe una instancia con la misma clave: %s".formatted(valorClave));
        });
    }

    public static <T> Either<Falla, T> eitherCatch(String contexto, CheckedFunction0<T> supplier) {
        try {
            return Either.right(supplier.apply());
        } catch (Exception e) {
            return Either.left(new FallaAplicacion("Error %s: %s".formatted(contexto, e.getMessage())));
        } catch (Throwable t) {
            return Either.left(new FallaSistema("Error inesperado: %s".formatted(t.getMessage()), t));
        }
    }

    public static <T> Either<Falla, T> eitherCatch(String contexto, T value, CheckedConsumer<T> consumer) {
        try {
            if (consumer != null) {
                consumer.accept(value);
            }
            return Either.right(value);
        } catch (Exception e) {
            return Either.left(new FallaAplicacion("Error %s: %s".formatted(contexto, e.getMessage())));
        } catch (Throwable t) {
            return Either.left(new FallaSistema("Error inesperado: %s".formatted(t.getMessage()), t));
        }
    }
}

