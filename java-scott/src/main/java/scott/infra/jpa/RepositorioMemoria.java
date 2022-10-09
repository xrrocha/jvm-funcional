package scott.infra.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import scott.infra.jpa.entidad.Entidad;
import scott.infra.jpa.entidad.EscuchaEntidad;
import scott.infra.util.Colecciones;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@SuppressWarnings("ALL")
public abstract class RepositorioMemoria<E extends Entidad> implements Repositorio<E> {

    private static final Logger logger = LoggerFactory.getLogger(RepositorioMemoria.class);

    final protected Map<String, E> instancias;

    public RepositorioMemoria() {
        this(new LinkedHashMap<>());
    }

    public RepositorioMemoria(Map<String, E> instancias) {
        this.instancias = instancias;
    }

    @Override
    public <S extends E> S save(S entidad) {
        EscuchaEntidad.prePersist(entidad);
        instancias.put(entidad.getId(), entidad);
        logger.debug("Guardando %s: %s [%s]".formatted(entidad.getClass().getSimpleName(), entidad.getId(), entidad));
        return entidad;
    }

    @Override
    public <S extends E> List<S> saveAll(Iterable<S> entidades) {
        return Colecciones.toStream(entidades)
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<E> findById(String id) {
        logger.debug("%s: buscando id %s: %s".formatted(getClass().getSimpleName(), id, instancias.containsKey(id)));
        return Optional.of(instancias.get(id));
    }

    @Override
    public boolean existsById(String id) {
        logger.debug("%s: verificando id %s: %s".formatted(getClass().getSimpleName(), id, instancias.containsKey(id)));
        return instancias.containsKey(id);
    }

    @Override
    public List<E> findAll() {
        logger.debug("%s: buscando todos: %d".formatted(getClass().getSimpleName(), instancias.size()));
        return new ArrayList<>(instancias.values());
    }

    @Override
    public List<E> findAllById(Iterable<String> ids) {
        logger.debug("%s: buscando ids: %s".formatted(getClass().getSimpleName(), ids));
        return Colecciones.toStream(ids)
                .map(instancias::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public long count() {
        return instancias.size();
    }

    @Override
    public void deleteById(String id) {
        logger.debug("%s: borrando id: %s".formatted(getClass().getSimpleName(), instancias.containsKey(id)));
        instancias.remove(id);
    }

    @Override
    public void delete(E entidad) {
        deleteById(entidad.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        logger.debug("%s: borrando ids: %s".formatted(getClass().getSimpleName(), ids));
        Colecciones.toStream(ids).forEach(instancias::remove);

    }

    @Override
    public void deleteAll(Iterable<? extends E> entidades) {
        Colecciones.toStream(entidades)
                .map(Entidad::getId)
                .forEach(instancias::remove);

    }

    @Override
    public void deleteAll() {
        logger.debug("Borrando todos: %d".formatted(instancias.size()));
        instancias.clear();
    }

    @Override
    public List<E> findAll(Sort sort) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends E> S saveAndFlush(S entidad) {
        save(entidad);
        flush();
        return entidad;
    }

    @Override
    public <S extends E> List<S> saveAllAndFlush(Iterable<S> entidades) {
        return StreamSupport
                .stream(entidades.spliterator(), false)
                .map(this::saveAndFlush)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllInBatch(Iterable<E> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<String> strings) {
        strings.forEach(this::deleteById);
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    @SuppressWarnings("deprecation")
    public E getOne(String s) {
        return getById(s);
    }

    @Override
    public E getById(String s) {
        return instancias.get(s);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends E> List<S> findAll(Example<S> example) {
        return instancias.values().stream().map(e -> (S) e).toList();
    }

    @Override
    public <S extends E> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public Page<E> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public <S extends E> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public <S extends E> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public <S extends E> long count(Example<S> example) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public <S extends E> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Unimplemented");
    }

    @Override
    public <S extends E, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Unimplemented");
    }
}
