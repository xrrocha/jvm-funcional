package scott.infra.util;

import java.util.UUID;

public class Identificadores {
    public static String siguienteIdentificador() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
