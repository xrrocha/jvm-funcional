package scott.dominio;

import scott.infra.jpa.ConvertidorEnumeracion;

public enum Genero {
    FEMENINO, MASCULINO;

    public static class ConvertidorGenero extends ConvertidorEnumeracion<Genero> {
        public ConvertidorGenero() {
            super(Genero.values(), em -> em.name().substring(0, 1));
        }
    }
}
