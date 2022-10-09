package scott.infra.util;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Colecciones {
    public static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }
}
