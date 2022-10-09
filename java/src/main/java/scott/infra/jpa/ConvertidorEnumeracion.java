package scott.infra.jpa;

import io.vavr.Tuple2;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public abstract class ConvertidorEnumeracion<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Map<E, String> porConstante;
    private final Map<String, E> porCadena;

    public ConvertidorEnumeracion(E[] values) {
        this(values, E::toString);
    }

    public ConvertidorEnumeracion(E[] values, Function<E, String> toString) {
        porConstante = Arrays.stream(values)
                .map(v -> new Tuple2<>(v, toString.apply(v)))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
        porCadena = porConstante.entrySet().stream()
                .map(entry -> new Tuple2<>(entry.getValue(), entry.getKey()))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }

    @Override
    public String convertToDatabaseColumn(E constante) {
        if (constante == null) {
            return null;
        }
        return porConstante.get(constante);
    }

    @Override
    public E convertToEntityAttribute(String cadena) {
        if (cadena == null) {
            return null;
        }
        final var constante = porCadena.get(cadena);
        if (constante == null) {
            throw new IllegalArgumentException("Constante inválida: %s".formatted(cadena));
        }
        return constante;
    }
}
