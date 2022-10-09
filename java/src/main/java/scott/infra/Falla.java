package scott.infra;

import scott.infra.jpa.entidad.ErrorValidacion;

public interface Falla {

    String mensaje();

    Throwable error();

    record FallaAplicacion(String mensaje, Throwable error) implements Falla {
        public FallaAplicacion(String mensaje) {
            this(mensaje, null);
        }

        public FallaAplicacion(Throwable error) {
            this(error.getMessage(), error);
        }
    }

    record FallaValidacion(String contexto, ErrorValidacion error) implements Falla {
        @Override
        public String mensaje() {
            return "%s: %s".formatted(contexto, error.getMessage());
        }
    }

    record FallaSistema(String contexto, Throwable error) implements Falla {

        @Override
        public String mensaje() {
            return "Error %s: %s".formatted(
                    contexto(),
                    error().getMessage() == null ? error().toString() : error().getMessage()
            );
        }
    }
}
